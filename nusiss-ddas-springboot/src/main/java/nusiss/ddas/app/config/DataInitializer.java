package nusiss.ddas.app.config;

import nusiss.ddas.app.model.Department;
import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.model.enums.DocumentType;
import nusiss.ddas.app.model.enums.Role;
import nusiss.ddas.app.repository.DepartmentRepository;
import nusiss.ddas.app.repository.DocumentRepository;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.util.FileHashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.storage.upload-dir}")
    private String uploadDir;

    // Local-dev defaults only; the prod profile requires these to come from the environment.
    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.user-password:user123}")
    private String userPassword;

    @Value("${app.seed.manager-password:manager123}")
    private String managerPassword;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            Department it = departmentRepository.findByDeptName("IT").orElseThrow();
            Department cs = departmentRepository.findByDeptName("CS").orElseThrow();

            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setEmail("admin@nus.edu.sg");
                admin.setRole(Role.ADMIN);
                admin.setDepartment(it);
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPasswordHash(passwordEncoder.encode(userPassword));
                user.setEmail("user@nus.edu.sg");
                user.setRole(Role.USER);
                user.setDepartment(cs);
                user.setCreatedBy(admin);
                userRepository.save(user);

                User approver = new User();
                approver.setUsername("manager");
                approver.setPasswordHash(passwordEncoder.encode(managerPassword));
                approver.setEmail("manager@nus.edu.sg");
                approver.setRole(Role.APPROVER);
                approver.setDepartment(cs);
                approver.setCreatedBy(admin);
                userRepository.save(approver);
            }

            if (documentRepository.count() == 0) {
                User user = userRepository.findByUsername("user").orElseThrow();
                User approver = userRepository.findByUsername("manager").orElseThrow();

                documentRepository.save(seedDocument(
                    "Annual Leave Application", DocumentType.LEAVE, user,
                    "Requesting 5 days of annual leave from 2026-09-14 to 2026-09-18."));

                documentRepository.save(seedDocument(
                    "Laptop Procurement Request", DocumentType.PROCUREMENT, approver,
                    "Requesting approval to purchase a replacement laptop for the CS department."));

                documentRepository.save(seedDocument(
                    "Medical Certificate Submission", DocumentType.MEDICAL, user,
                    "Submitting medical certificate for sick leave taken on 2026-08-29."));
            }
        };
    }

    private Document seedDocument(String docName, DocumentType docType, User uploader, String content) throws IOException {
        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String fileName = docType.name().toLowerCase() + "-" + System.nanoTime() + ".txt";
        Path filePath = dir.resolve(fileName);
        Files.write(filePath, bytes);

        String fileHash = FileHashUtil.sha256Hex(bytes);
        return new Document(docName, docType, uploader, filePath.toString(), fileHash, bytes.length, "text/plain");
    }
}
