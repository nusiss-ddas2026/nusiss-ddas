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
                manager.setRole("MANAGER");
                manager.setDept("CS");
                userRepository.save(manager);
            }

            if (documentRepository.count() == 0) {
                Document doc1 = new Document();
                doc1.setDocumentName("New user application request");
                doc1.setDocumentType("Request");
                doc1.setUploader("user");
                doc1.setStatus("PENDING");
                doc1.setCreatedOn(LocalDateTime.of(2026, 8, 29, 17, 20, 0));
                documentRepository.save(doc1);

                Document doc2 = new Document();
                doc2.setDocumentName("Request for increase funding to my Personal bank account");
                doc2.setDocumentType("Request");
                doc2.setUploader("manager");
                doc2.setStatus("PENDING");
                doc2.setCreatedOn(LocalDateTime.of(2026, 8, 30, 4, 44, 44));
                documentRepository.save(doc2);
            }
        };
    }
}