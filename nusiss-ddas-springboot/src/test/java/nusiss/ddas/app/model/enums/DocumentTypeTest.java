package nusiss.ddas.app.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTypeTest {

    @ParameterizedTest
    @CsvSource({"1,LEAVE", "2,PROCUREMENT", "3,MEDICAL"})
    void fromCodeReturnsMatchingType(int code, DocumentType expected) {
        assertThat(DocumentType.fromCode(code)).isEqualTo(expected);
    }

    @Test
    void getCodeRoundTripsThroughFromCode() {
        for (DocumentType type : DocumentType.values()) {
            assertThat(DocumentType.fromCode(type.getCode())).isEqualTo(type);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, -1})
    void fromCodeRejectsUnknownCodes(int code) {
        assertThatThrownBy(() -> DocumentType.fromCode(code))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown document type code");
    }
}
