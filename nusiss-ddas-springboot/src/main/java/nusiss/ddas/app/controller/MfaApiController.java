package nusiss.ddas.app.controller;

import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Self-service MFA enrollment for the currently logged-in user: check status,
 * set up a new TOTP secret + QR code, confirm it, or turn MFA back off. This is
 * account management, not login - kept separate from AuthApiController, and
 * authenticated rather than permitAll (it falls under the existing
 * "/api/**" -> authenticated() rule in SecurityConfig).
 */
@RestController
@RequestMapping("/api/mfa")
public class MfaApiController {

    private final UserRepository userRepository;
    private final TotpService totpService;

    public MfaApiController(UserRepository userRepository, TotpService totpService) {
        this.userRepository = userRepository;
        this.totpService = totpService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(Authentication auth) {
        User user = currentUser(auth);
        return ResponseEntity.ok(Map.of("mfaEnabled", user.isMfaEnabled()));
    }

    /**
     * Generates and persists a new (not-yet-enabled) secret for the current user,
     * returning the QR code to scan plus the raw secret for manual entry. Calling
     * this again before /enable replaces the pending secret - the frontend only
     * calls this behind an explicit "Enable MFA" action, not automatically on
     * page load, since it mutates state.
     */
    @GetMapping("/setup")
    public ResponseEntity<?> setup(Authentication auth) {
        User user = currentUser(auth);
        try {
            String secret = totpService.generateSecret();
            user.setMfaSecret(secret);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "secret", secret,
                "qrCodeDataUri", totpService.buildQrDataUri(user.getUsername(), secret),
                "otpAuthUrl", totpService.buildOtpAuthUrl(user.getUsername(), secret)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not start MFA setup. Please try again."));
        }
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable(@RequestBody Map<String, String> body, Authentication auth) {
        User user = currentUser(auth);
        if (user.getMfaSecret() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Call /api/mfa/setup first"));
        }
        if (!totpService.verifyCode(user.getMfaSecret(), body.get("code"))) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid authentication code"));
        }
        user.setMfaEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "MFA enabled"));
    }

    /**
     * Turns MFA off for the current user. Simplification accepted for this demo:
     * does not re-verify the password or a current TOTP code first - a production
     * system should require one of those before allowing MFA to be disabled.
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disable(Authentication auth) {
        User user = currentUser(auth);
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "MFA disabled"));
    }
}
