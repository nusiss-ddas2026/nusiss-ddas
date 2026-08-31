package nusiss.ddas.app.config;

import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.DocumentRepository;
import nusiss.ddas.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@nus.edu.sg");
                admin.setRole("ADMIN");
                admin.setDept("IT");
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEmail("user@nus.edu.sg");
                user.setRole("USER");
                user.setDept("CS");
                userRepository.save(user);

                User manager = new User();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager@nus.edu.sg");
                manager.setRole("APPROVER");
                manager.setDept("CS");
                userRepository.save(manager);
            }

            if (documentRepository.count() == 0) {
                Document doc = new Document();
                doc.setDocumentName("New user application request");
                doc.setDocumentType("Request");
                doc.setUploader("user");
                doc.setStatus("PENDING");
                doc.setCreatedOn(LocalDateTime.of(2026, 8, 29, 17, 20, 0));
                documentRepository.save(doc);
            }
        };
    }
}