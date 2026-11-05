package nusiss.ddas.app.controller;

import nusiss.ddas.app.model.JwtToken;
import nusiss.ddas.app.model.User;
import nusiss.ddas.app.model.enums.AuthProvider;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.JwtTokenService;
import nusiss.ddas.app.service.MfaChallengeService;
import nusiss.ddas.app.service.TokenIssuanceService;
import nusiss.ddas.app.service.TotpService;
import nusiss.ddas.app.service.UserService;
import nusiss.ddas.app.util.JwtUtil;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final MfaChallengeService mfaChallengeService;
    private final TotpService totpService;
    private final TokenIssuanceService tokenIssuanceService;

    public AuthApiController(UserService userService, UserDetailsService userDetailsService,
                             JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder,
                             JwtTokenService jwtTokenService, MfaChallengeService mfaChallengeService,
                             TotpService totpService, TokenIssuanceService tokenIssuanceService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.mfaChallengeService = mfaChallengeService;
        this.totpService = totpService;
        this.tokenIssuanceService = tokenIssuanceService;
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAuthProvider() == AuthProvider.OIDC) {
            return ResponseEntity.status(401).body(Map.of("error",
                    "This account uses Single Sign-On. Please log in with the Keycloak button."));
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        if (user.isMfaEnabled()) {
            // Password is correct, but a second factor is still required. No tokens are
            // issued yet, and nothing is written to jwt_tokens - see MfaChallengeService
            // for why a pending login must stay out of that table entirely.
            String challengeId = mfaChallengeService.createChallenge(user.getUserId());
            Map<String, Object> mfaResponse = new HashMap<>();
            mfaResponse.put("mfaRequired", true);
            mfaResponse.put("challengeId", challengeId);
            return ResponseEntity.ok(mfaResponse);
        }

        return ResponseEntity.ok(tokenIssuanceService.issueTokensAndSetCookie(user, userDetails, response));
    }

    /**
     * Completes a login that was paused for MFA: exchanges a valid challengeId
     * (proof that the password step already succeeded) plus a correct TOTP code
     * for the normal access/refresh token pair, issued exactly as a non-MFA login
     * would issue them.
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String challengeId = body.get("challengeId");
        String code = body.get("code");

        Integer userId = mfaChallengeService.peekChallenge(challengeId);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Login session expired or invalid. Please log in again."));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Login session expired or invalid. Please log in again."));
        }

        if (!totpService.verifyCode(user.getMfaSecret(), code)) {
            // Wrong code: leave the challenge alive so the user can retry without
            // having to re-enter their password.
            return ResponseEntity.status(401).body(Map.of("error", "Invalid authentication code"));
        }

        mfaChallengeService.invalidateChallenge(challengeId);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return ResponseEntity.ok(tokenIssuanceService.issueTokensAndSetCookie(user, userDetails, response));
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
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}