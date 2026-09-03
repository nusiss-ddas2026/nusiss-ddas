package nusiss.ddas.app.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nusiss.ddas.app.model.enums.DocumentStatus;

@Converter(autoApply = true)
public class DocumentStatusConverter implements AttributeConverter<DocumentStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DocumentStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public DocumentStatus convertToEntityAttribute(Integer code) {
        return code == null ? null : DocumentStatus.fromCode(code);
    }
}
