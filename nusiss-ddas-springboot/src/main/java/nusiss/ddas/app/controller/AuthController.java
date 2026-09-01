package nusiss.ddas.app.controller;

import nusiss.ddas.app.model.Approval;
import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.ApprovalRepository;
import nusiss.ddas.app.repository.DocumentRepository;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
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
    private ApprovalRepository approvalRepository;

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

        List<Document> myDocuments = documentRepository.findByUploader(username);
        model.addAttribute("myDocuments", myDocuments);

        if (currentUser != null) {
            List<User> deptUsers = userRepository.findByDept(currentUser.getDept());
            List<String> deptUsernames = deptUsers.stream().map(User::getUsername).collect(Collectors.toList());
            List<Document> deptDocuments = documentRepository.findByUploaderIn(deptUsernames).stream()
                .filter(d -> !d.getUploader().equals(username)).collect(Collectors.toList());
            deptDocuments.sort((a, b) -> {
                if ("PENDING".equals(a.getStatus()) && !"PENDING".equals(b.getStatus())) return -1;
                if (!"PENDING".equals(a.getStatus()) && "PENDING".equals(b.getStatus())) return 1;
                return 0;
            });
            model.addAttribute("deptDocuments", deptDocuments);
            model.addAttribute("userRole", currentUser.getRole());
            model.addAttribute("userDept", currentUser.getDept());
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("username", username);
        model.addAttribute("activeMode", mode != null ? mode : "myDocuments");
        return "home";
    }

    @PostMapping("/reviewDocument")
    public String reviewDocument(@RequestParam Integer documentId,
                                 @RequestParam String action,
                                 @RequestParam(defaultValue = "requestManagement") String mode,
                                 Authentication auth) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc != null) {
            if ("APPROVE".equals(action)) doc.setStatus("APPROVED");
            else if ("REJECT".equals(action)) doc.setStatus("REJECTED");
            else if ("REVIEW".equals(action)) doc.setStatus("REVIEWING");
            documentRepository.save(doc);

            Approval approval = new Approval();
            approval.setDocumentId(documentId);
            approval.setApprover(auth.getName());
            approval.setSignature("1111".getBytes());
            approval.setCreatedOn(LocalDateTime.now());
            approvalRepository.save(approval);
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/updateRole")
    public String updateRole(@RequestParam Long userId, @RequestParam String role,
                             @RequestParam(defaultValue = "myDocuments") String mode) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRole(role);
            userRepository.save(user);
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/updateDept")
    public String updateDept(@RequestParam Long userId, @RequestParam String dept,
                             @RequestParam(defaultValue = "myDocuments") String mode) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setDept(dept);
            userRepository.save(user);
        }
        return "redirect:/home?mode=" + mode;
    }

    @PostMapping("/bulkUpdateUsers")
    public String bulkUpdateUsers(@RequestParam("userId") Long[] userId,
                                  @RequestParam("role") String[] role,
                                  @RequestParam("dept") String[] dept,
                                  @RequestParam(defaultValue = "myDocuments") String mode) {
        for (int i = 0; i < userId.length; i++) {
            User user = userRepository.findById(userId[i]).orElse(null);
            if (user != null) {
                user.setRole(role[i]);
                user.setDept(dept[i]);
                userRepository.save(user);
            }
        }
        return "redirect:/home?mode=" + mode;
    }
}