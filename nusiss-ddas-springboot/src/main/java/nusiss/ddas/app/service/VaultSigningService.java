package nusiss.ddas.app.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.Signature;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultTransitKeyCreationRequest;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;

@Service
public class VaultSigningService {

    private static final Logger log = LoggerFactory.getLogger(VaultSigningService.class);
    private static final String TRANSIT_MOUNT_PATH = "transit";
    private static final String TRANSIT_KEY_TYPE = "rsa-2048";

    @Autowired
    private VaultTemplate vaultTemplate;

    @Value("${app.vault.transit.key-name}")
    private String transitKeyName;

    @PostConstruct
    public void initTransitBackend() {
        try {
            boolean transitMounted = vaultTemplate.opsForSys().getMounts().keySet().stream()
                .map(path -> path.endsWith("/") ? path.substring(0, path.length() - 1) : path)
                .anyMatch(TRANSIT_MOUNT_PATH::equals);

            if (!transitMounted) {
                vaultTemplate.opsForSys().mount(TRANSIT_MOUNT_PATH, VaultMount.create(TRANSIT_MOUNT_PATH));
            }

            if (vaultTemplate.opsForTransit().getKey(transitKeyName) == null) {
                vaultTemplate.opsForTransit().createKey(
                    transitKeyName, VaultTransitKeyCreationRequest.ofKeyType(TRANSIT_KEY_TYPE));
            }
        } catch (VaultException | RestClientException e) {
            log.warn("Could not initialize Vault transit backend at startup; "
                + "document approval/signing will fail until this is resolved.", e);
        }
    }

    /**
     * Signs the given SHA-256 hex digest string using this app's Vault transit
     * key. The hex string's UTF-8 bytes are what gets signed (Vault base64-encodes
     * them on the wire internally — callers must not pre-encode). Returns Vault's
     * own signature format, e.g. "vault:v1:MEUCIQ...".
     */
    public String sign(String hashHex) {
        Plaintext plaintext = Plaintext.of(hashHex, StandardCharsets.UTF_8);
        Signature signature = vaultTemplate.opsForTransit().sign(transitKeyName, plaintext);
        return signature.getSignature();
    }
}
