package nusiss.ddas.app.model.converter;

import org.junit.jupiter.api.Test;

import nusiss.ddas.app.model.enums.DocumentStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentStatusConverterTest {

    private final DocumentStatusConverter converter = new DocumentStatusConverter();

    @Test
    void convertsStatusToItsDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(DocumentStatus.PENDING)).isEqualTo(1);
        assertThat(converter.convertToDatabaseColumn(DocumentStatus.APPROVED)).isEqualTo(2);
        assertThat(converter.convertToDatabaseColumn(DocumentStatus.REJECTED)).isEqualTo(3);
        assertThat(converter.convertToDatabaseColumn(DocumentStatus.REVIEWING)).isEqualTo(4);
    }

    @Test
    void convertsDatabaseCodeBackToStatus() {
        assertThat(converter.convertToEntityAttribute(1)).isEqualTo(DocumentStatus.PENDING);
        assertThat(converter.convertToEntityAttribute(4)).isEqualTo(DocumentStatus.REVIEWING);
    }

    @Test
    void handlesNullsOnBothSides() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void rejectsUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(99))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
