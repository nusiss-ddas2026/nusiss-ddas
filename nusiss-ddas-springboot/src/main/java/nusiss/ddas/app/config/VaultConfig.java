package nusiss.ddas.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

/**
 * Generic Vault connectivity configuration. Produces a single shared
 * VaultTemplate bean for use by other components in the application.
 */
@Configuration
public class VaultConfig {

    @Value("${app.vault.uri}")
    private String vaultUri;

    @Value("${app.vault.token}")
    private String vaultToken;

    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
        return new VaultTemplate(endpoint, new TokenAuthentication(vaultToken));
    }
}
