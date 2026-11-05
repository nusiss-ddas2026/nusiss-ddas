package nusiss.ddas.app.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void generateSecretReturnsNonEmptyBase32Secret() {
        String secret = totpService.generateSecret();
        assertThat(secret).isNotBlank();
        assertThat(Base64.getDecoder().decode(secret)).isNotEmpty();
    }

    @Test
    void generateSecretReturnsDifferentSecretEachTime() {
        assertThat(totpService.generateSecret()).isNotEqualTo(totpService.generateSecret());
    }

    @Test
    void buildOtpAuthUrlContainsIssuerAndLabel() {
        String url = totpService.buildOtpAuthUrl("alice@example.com", "JBSWY3DPEHPK3PXP");
        assertThat(url)
            .startsWith("otpauth://totp/")
            .contains("issuer=DDAS")
            .contains("secret=JBSWY3DPEHPK3PXP");
    }

    @Test
    void buildQrDataUriReturnsBase64PngDataUri() {
        String dataUri = totpService.buildQrDataUri("alice@example.com", "JBSWY3DPEHPK3PXP");
        assertThat(dataUri).startsWith("data:image/png;base64,");
        byte[] png = Base64.getDecoder().decode(dataUri.substring("data:image/png;base64,".length()));
        assertThat(png).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
    }

    @Test
    void verifyCodeRejectsNullSecretOrCode() {
        assertThat(totpService.verifyCode(null, "123456")).isFalse();
        assertThat(totpService.verifyCode("JBSWY3DPEHPK3PXP", null)).isFalse();
    }

    @Test
    void verifyCodeRejectsMalformedCode() {
        assertThat(totpService.verifyCode("JBSWY3DPEHPK3PXP", "not-a-code")).isFalse();
    }
}
