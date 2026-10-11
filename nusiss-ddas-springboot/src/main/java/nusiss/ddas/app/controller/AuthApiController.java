package nusiss.ddas.app.controller;

import nusiss.ddas.app.model.JwtToken;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.JwtTokenService;
import nusiss.ddas.app.service.UserService;
import nusiss.ddas.app.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private static final Logger log = LoggerFactory.getLogger(AuthApiController.class);

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthApiController(UserService userService, UserDetailsService userDetailsService,
                             JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder,
                             JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");

        try {
            userService.registerUser(username, password, email);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request,
                                   HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("SECURITY_EVENT type=LOGIN_FAILURE user={} reason=unknown_user ip={}",
                    sanitizeForLog(username), request.getRemoteAddr());
            throw e;
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("SECURITY_EVENT type=LOGIN_FAILURE user={} reason=bad_password ip={}",
                    sanitizeForLog(username), request.getRemoteAddr());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        log.info("SECURITY_EVENT type=LOGIN_SUCCESS user={} ip={}",
                sanitizeForLog(username), request.getRemoteAddr());

        jwtTokenService.invalidateUserTokens(user.getUserId());

        String accessToken = jwtUtil.generateToken(userDetails, new HashMap<String, Object>() {{
            put("role", user.getRole().name());
        }});
        String refreshToken = jwtUtil.generateRefreshToken(userDetails, new HashMap<>());

        jwtTokenService.saveToken(user.getUserId(), accessToken, "ACCESS");
        jwtTokenService.saveToken(user.getUserId(), refreshToken, "REFRESH");

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", accessToken)
                .httpOnly(false) // jwt-helper.js reads this cookie; needs a frontend change to harden
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(24 * 60 * 60)
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("role", user.getRole().name());
        tokens.put("username", username);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        JwtToken tokenRecord = jwtTokenService.findTokenByToken(refreshToken);
        if (tokenRecord == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token expired"));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(userDetails, new HashMap<String, Object>() {{
            put("role", user.getRole().name());
        }});

        jwtTokenService.saveToken(user.getUserId(), newAccessToken, "ACCESS");

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        ResponseCookie deleteCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(false)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /** Strips CR/LF so a crafted username can't inject fake lines into the security log. */
    private static String sanitizeForLog(String value) {
        return value == null ? "null" : value.replaceAll("[\\r\\n]", "_");
    }
}
