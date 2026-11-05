package nusiss.ddas.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Split out from SecurityConfig: SecurityConfig field-injects OidcAuthenticationSuccessHandler
 * (for oauth2Login), which itself needs PasswordEncoder - defining the bean inside SecurityConfig
 * created a circular dependency (SecurityConfig -> OidcAuthenticationSuccessHandler -> PasswordEncoder
 * -> SecurityConfig). Keeping it in its own configuration class breaks the cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
