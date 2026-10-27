package nusiss.ddas.app.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentStatusTest {

    @ParameterizedTest
    @CsvSource({"1,PENDING", "2,APPROVED", "3,REJECTED", "4,REVIEWING"})
    void fromCodeReturnsMatchingStatus(int code, DocumentStatus expected) {
        assertThat(DocumentStatus.fromCode(code)).isEqualTo(expected);
    }

    @Test
    void getCodeRoundTripsThroughFromCode() {
        for (DocumentStatus status : DocumentStatus.values()) {
            assertThat(DocumentStatus.fromCode(status.getCode())).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, -3})
    void fromCodeRejectsUnknownCodes(int code) {
        assertThatThrownBy(() -> DocumentStatus.fromCode(code))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown document status code");
    }
}
