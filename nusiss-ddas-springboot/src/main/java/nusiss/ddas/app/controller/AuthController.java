package nusiss.ddas.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import nusiss.ddas.app.exception.InvalidFileException;
import nusiss.ddas.app.model.AuditLog;
import nusiss.ddas.app.model.Department;
import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.DocumentApproval;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.model.enums.DocumentStatus;
import nusiss.ddas.app.model.enums.DocumentType;
import nusiss.ddas.app.model.enums.Role;
import nusiss.ddas.app.repository.AuditLogRepository;
import nusiss.ddas.app.repository.DepartmentRepository;
import nusiss.ddas.app.repository.DocumentApprovalRepository;
import nusiss.ddas.app.repository.DocumentRepository;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.FileStorageService;
import nusiss.ddas.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DocumentApprovalRepository documentApprovalRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail());
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/home")
    public String homePage(Model model, Authentication auth,
                           @RequestParam(required = false) String mode) {
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        List<Document> myDocuments = documentRepository.findByUploader(currentUser);
        model.addAttribute("myDocuments", myDocuments);

        if (currentUser != null && currentUser.getDepartment() != null) {
            List<User> deptUsers = userRepository.findByDepartment(currentUser.getDepartment());
            List<Document> deptDocuments = documentRepository.findByUploaderIn(deptUsers).stream()
                .filter(d -> !d.getUploader().getUsername().equals(username)).collect(Collectors.toList());
            deptDocuments.sort((a, b) -> {
                boolean aPending = a.getStatus() == DocumentStatus.PENDING;
                boolean bPending = b.getStatus() == DocumentStatus.PENDING;
                if (aPending && !bPending) return -1;
                if (!aPending && bPending) return 1;
                return 0;
            });
            model.addAttribute("deptDocuments", deptDocuments);
            model.addAttribute("userRole", currentUser.getRole().name());
            model.addAttribute("userDept", currentUser.getDepartment().getDeptName());
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("username", username);
        model.addAttribute("activeMode", mode != null ? mode : "myDocuments");
        return "home";
    }

    @GetMapping("/uploadDocument")
    public String uploadDocumentPage() {
        return "upload";
    }

    @PostMapping("/uploadDocument")
    public String uploadDocument(@RequestParam String docName,
                                 @RequestParam String docType,
                                 @RequestParam("file") MultipartFile file,
                                 Authentication auth,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        User uploader = userRepository.findByUsername(auth.getName()).orElse(null);
        try {
            DocumentType type = DocumentType.valueOf(docType);
            FileStorageService.StoredFile stored = fileStorageService.store(file);

            Document doc = new Document(docName, type, uploader, stored.filePath(), stored.fileHash(),
                                         stored.size(), stored.contentType());
            documentRepository.save(doc);

            auditLogRepository.save(new AuditLog(uploader, "UPLOADED document #" + doc.getDocId(),
                                                  doc, request.getRemoteAddr()));

            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully.");
            return "redirect:/home?mode=myDocuments";
        } catch (InvalidFileException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid document type selected.");
        }
        return "redirect:/uploadDocument";
    }

    @PostMapping("/reviewDocument")
    public String reviewDocument(@RequestParam Integer documentId,
                                 @RequestParam String action,
                                 @RequestParam(defaultValue = "requestManagement") String mode,
                                 Authentication auth,
                                 HttpServletRequest request) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        User actor = userRepository.findByUsername(auth.getName()).orElse(null);
        if (doc != null && actor != null) {
            String actionLabel;
            if ("APPROVE".equals(action)) {
                doc.setStatus(DocumentStatus.APPROVED);
                documentRepository.save(doc);

                // TODO: replace with a real HashiCorp Vault transit signature once Vault is wired up.
                String signatureValue = "PLACEHOLDER-SIGNATURE:" + UUID.randomUUID();
                DocumentApproval approval = new DocumentApproval(doc, actor, signatureValue, doc.getFileHash());
                documentApprovalRepository.save(approval);
                actionLabel = "APPROVED document #" + documentId;
            } else if ("REJECT".equals(action)) {
                doc.setStatus(DocumentStatus.REJECTED);
                documentRepository.save(doc);
                actionLabel = "REJECTED document #" + documentId;
            } else {
                doc.setStatus(DocumentStatus.REVIEWING);
                documentRepository.save(doc);
                actionLabel = "MARKED REVIEWING document #" + documentId;
            }

            auditLogRepository.save(new AuditLog(actor, actionLabel, doc, request.getRemoteAddr()));
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/updateRole")
    public String updateRole(@RequestParam Integer userId, @RequestParam String role,
                             @RequestParam(defaultValue = "myDocuments") String mode) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRole(Role.valueOf(role));
            userRepository.save(user);
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/updateDept")
    public String updateDept(@RequestParam Integer userId, @RequestParam String dept,
                             @RequestParam(defaultValue = "myDocuments") String mode) {
        User user = userRepository.findById(userId).orElse(null);
        Department department = departmentRepository.findByDeptName(dept).orElse(null);
        if (user != null && department != null) {
            user.setDepartment(department);
            userRepository.save(user);
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/bulkUpdateUsers")
    public String bulkUpdateUsers(@RequestParam("userId") Integer[] userId,
                                  @RequestParam("role") String[] role,
                                  @RequestParam("dept") String[] dept,
                                  @RequestParam(defaultValue = "myDocuments") String mode) {
        for (int i = 0; i < userId.length; i++) {
            User user = userRepository.findById(userId[i]).orElse(null);
            Department department = departmentRepository.findByDeptName(dept[i]).orElse(null);
            if (user != null && department != null) {
                user.setRole(Role.valueOf(role[i]));
                user.setDepartment(department);
                userRepository.save(user);
            }
        }
        return "redirect:/home?mode=" + mode;
    }
}
