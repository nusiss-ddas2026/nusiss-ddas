package nusiss.ddas.app.model.converter;

import org.junit.jupiter.api.Test;

import nusiss.ddas.app.model.enums.DocumentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTypeConverterTest {

    private final DocumentTypeConverter converter = new DocumentTypeConverter();

    @Test
    void convertsTypeToItsDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(DocumentType.LEAVE)).isEqualTo(1);
        assertThat(converter.convertToDatabaseColumn(DocumentType.PROCUREMENT)).isEqualTo(2);
        assertThat(converter.convertToDatabaseColumn(DocumentType.MEDICAL)).isEqualTo(3);
    }

    @Test
    void convertsDatabaseCodeBackToType() {
        assertThat(converter.convertToEntityAttribute(2)).isEqualTo(DocumentType.PROCUREMENT);
        assertThat(converter.convertToEntityAttribute(3)).isEqualTo(DocumentType.MEDICAL);
    }

    @Test
    void handlesNullsOnBothSides() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void rejectsUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(-5))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
