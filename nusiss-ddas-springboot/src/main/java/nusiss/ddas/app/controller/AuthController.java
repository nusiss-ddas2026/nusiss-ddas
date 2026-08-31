package nusiss.ddas.app.controller;

import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.DocumentRepository;
import nusiss.ddas.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRepository documentRepository;

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
    public String homePage(Model model, Authentication auth) {
        String username = auth.getName();
        List<Document> documents = documentRepository.findByUploader(username);
        model.addAttribute("documents", documents);
        model.addAttribute("username", username);
        return "home";
    }
}
