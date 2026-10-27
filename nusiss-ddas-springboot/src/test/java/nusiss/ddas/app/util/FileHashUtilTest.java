package nusiss.ddas.app.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FileHashUtilTest {

    @Test
    void sha256HexMatchesKnownVectorForAbc() {
        String hash = FileHashUtil.sha256Hex("abc".getBytes(StandardCharsets.UTF_8));
        assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sha256HexMatchesKnownVectorForEmptyInput() {
        String hash = FileHashUtil.sha256Hex(new byte[0]);
        assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void sha256HexIsLowercaseHexOfLength64() {
        String hash = FileHashUtil.sha256Hex("some document content".getBytes(StandardCharsets.UTF_8));
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void sha256HexIsDeterministicAndSensitiveToInput() {
        byte[] data = "same bytes".getBytes(StandardCharsets.UTF_8);
        assertThat(FileHashUtil.sha256Hex(data)).isEqualTo(FileHashUtil.sha256Hex(data.clone()));
        assertThat(FileHashUtil.sha256Hex(data))
            .isNotEqualTo(FileHashUtil.sha256Hex("same bytes!".getBytes(StandardCharsets.UTF_8)));
    }
}
