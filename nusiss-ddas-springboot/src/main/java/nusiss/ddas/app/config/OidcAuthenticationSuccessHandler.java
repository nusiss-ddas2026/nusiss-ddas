package nusiss.ddas.app.config;

import nusiss.ddas.app.model.User;
import nusiss.ddas.app.model.enums.AuthProvider;
import nusiss.ddas.app.model.enums.Role;
import nusiss.ddas.app.repository.UserRepository;
import nusiss.ddas.app.service.TokenIssuanceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Resolves (or provisions) the local User for a Keycloak-authenticated identity, then
 * mints the app's own JWT via the same chokepoint password/MFA login uses. MFA for this
 * path is enforced entirely by Keycloak's own login flow (required OTP action on the
 * realm) - the app never runs an OIDC login through its local TotpService.
 */
@Component
public class OidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final TokenIssuanceService tokenIssuanceService;
    private final PasswordEncoder passwordEncoder;

    public OidcAuthenticationSuccessHandler(UserRepository userRepository,
                                             UserDetailsService userDetailsService,
                                             TokenIssuanceService tokenIssuanceService,
                                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.tokenIssuanceService = tokenIssuanceService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String email = oidcUser.getEmail();
        Boolean emailVerified = oidcUser.getEmailVerified();
        if (email == null || emailVerified == null || !emailVerified) {
            response.sendRedirect("/login?error=oidc");
            return;
        }

        Optional<User> existingOidcUser = userRepository.findByEmailAndAuthProvider(email, AuthProvider.OIDC);
        User user;
        if (existingOidcUser.isPresent()) {
            user = existingOidcUser.get();
        } else {
            Optional<User> existingLocalUser = userRepository.findByEmail(email);
            if (existingLocalUser.isPresent() && existingLocalUser.get().getAuthProvider() == AuthProvider.LOCAL) {
                response.sendRedirect("/login?error=oidc_email_taken");
                return;
            }
            user = provisionUser(email, oidcUser.getPreferredUsername());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        tokenIssuanceService.issueTokensAndSetCookie(user, userDetails, response);
        response.sendRedirect("/home");
    }

    private User provisionUser(String email, String preferredUsername) {
        String baseUsername = (preferredUsername != null && !preferredUsername.isBlank())
                ? preferredUsername
                : email.substring(0, email.indexOf('@'));

        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setRole(Role.USER);
        user.setAuthProvider(AuthProvider.OIDC);
        return userRepository.save(user);
    }
}
