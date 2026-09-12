package nusiss.ddas.app.service;

import jakarta.annotation.PostConstruct;
import nusiss.ddas.app.model.User;
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
import java.util.regex.Pattern;

@Service
public class VaultSigningService {

    private static final Logger log = LoggerFactory.getLogger(VaultSigningService.class);
    private static final String TRANSIT_MOUNT_PATH = "transit";
    private static final String TRANSIT_KEY_TYPE = "rsa-2048";
    private static final Pattern UNSAFE_KEY_NAME_CHARS = Pattern.compile("[^a-zA-Z0-9_-]");

    @Autowired
    private VaultTemplate vaultTemplate;

    @Value("${app.vault.transit.key-name-prefix}")
    private String transitKeyNamePrefix;

    @PostConstruct
    public void initTransitBackend() {
        try {
            boolean transitMounted = vaultTemplate.opsForSys().getMounts().keySet().stream()
                .map(path -> path.endsWith("/") ? path.substring(0, path.length() - 1) : path)
                .anyMatch(TRANSIT_MOUNT_PATH::equals);

            if (!transitMounted) {
                vaultTemplate.opsForSys().mount(TRANSIT_MOUNT_PATH, VaultMount.create(TRANSIT_MOUNT_PATH));
            }
        } catch (VaultException | RestClientException e) {
            log.warn("Could not initialize Vault transit backend at startup; "
                + "document approval/signing will fail until this is resolved.", e);
        }
    }

    /**
     * Replaces every character outside [a-zA-Z0-9_-] with "_" so a username is
     * safe to use as a Vault transit key name (a literal URL path segment).
     * Not collision-resistant: two different usernames that differ only in a
     * replaced character (e.g. "bob smith" and "bob_smith") sanitize to the
     * same key name and would share a signing key. Accepted limitation for
     * this project — usernames are unique at the DB level, but sanitized
     * collisions between two distinct usernames aren't detected or prevented.
     */
    private String sanitizeForKeyName(String username) {
        return UNSAFE_KEY_NAME_CHARS.matcher(username).replaceAll("_");
    }

    private String keyNameFor(User approver) {
        return transitKeyNamePrefix + "-" + sanitizeForKeyName(approver.getUsername());
    }

    private void ensureKeyExists(String keyName) {
        if (vaultTemplate.opsForTransit().getKey(keyName) == null) {
            vaultTemplate.opsForTransit().createKey(
                keyName, VaultTransitKeyCreationRequest.ofKeyType(TRANSIT_KEY_TYPE));
        }
    }

    public record SignedResult(String signatureValue, String keyName) {}

    /**
     * Signs the given SHA-256 hex digest string using the given approver's own
     * Vault transit key, creating that key on first use if it doesn't exist
     * yet. The hex string's UTF-8 bytes are what gets signed (Vault
     * base64-encodes them on the wire internally — callers must not
     * pre-encode). Returns Vault's own signature format (e.g.
     * "vault:v1:MEUCIQ...") together with the exact key name used.
     */
    public SignedResult sign(User approver, String hashHex) {
        String keyName = keyNameFor(approver);
        ensureKeyExists(keyName);
        Plaintext plaintext = Plaintext.of(hashHex, StandardCharsets.UTF_8);
        Signature signature = vaultTemplate.opsForTransit().sign(keyName, plaintext);
        return new SignedResult(signature.getSignature(), keyName);
    }

    /**
     * Verifies that signatureValue is a genuine signature over hashHex produced by the
     * transit key named keyName — false if the signature was forged, corrupted, or
     * produced by a different key (e.g. a different approver's key than the one on
     * record for this approval). keyName should come from the stored
     * DocumentApproval.signingKeyName rather than being re-derived from a username,
     * so verification checks against the exact key that was actually used to sign.
     */
    public boolean verify(String keyName, String hashHex, String signatureValue) {
        Plaintext plaintext = Plaintext.of(hashHex, StandardCharsets.UTF_8);
        return vaultTemplate.opsForTransit().verify(keyName, plaintext, Signature.of(signatureValue));
    }
}
