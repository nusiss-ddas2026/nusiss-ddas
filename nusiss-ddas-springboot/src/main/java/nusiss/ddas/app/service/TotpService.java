package nusiss.ddas.app.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Wraps dev.samstevens.totp (RFC 6238 TOTP, Google/Microsoft Authenticator and
 * Authy compatible) to generate per-user secrets, render enrollment QR codes,
 * and verify submitted 6-digit codes. Entirely offline - no external service
 * calls, no cost.
 */
@Service
public class TotpService {

    private static final String ISSUER = "DDAS";
    private static final QrGenerator QR_GENERATOR = new ZxingPngQrGenerator();
    private static final CodeVerifier CODE_VERIFIER =
        new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    public String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }

    private QrData buildQrData(String username, String secret) {
        return new QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer(ISSUER)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();
    }

    /**
     * Returns the enrollment QR code as a base64-encoded PNG data URI, safe to
     * inline directly into an &lt;img src="..."&gt; tag (the app's CSP already
     * allows img-src 'self' data:;).
     */
    public String buildQrDataUri(String username, String secret) {
        try {
            byte[] png = QR_GENERATOR.generate(buildQrData(username, secret));
            return "data:" + QR_GENERATOR.getImageMimeType() + ";base64," + Base64.getEncoder().encodeToString(png);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Could not generate MFA enrollment QR code", e);
        }
    }

    /** The raw otpauth:// URI, for manual entry into an authenticator app that can't scan a QR code. */
    public String buildOtpAuthUrl(String username, String secret) {
        return buildQrData(username, secret).getUri();
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        return CODE_VERIFIER.isValidCode(secret, code);
    }
}
