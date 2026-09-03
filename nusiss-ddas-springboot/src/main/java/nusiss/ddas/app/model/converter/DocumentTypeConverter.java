package nusiss.ddas.app.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nusiss.ddas.app.model.enums.DocumentType;

@Converter(autoApply = true)
public class DocumentTypeConverter implements AttributeConverter<DocumentType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DocumentType type) {
        return type == null ? null : type.getCode();
    }

    @Override
    public DocumentType convertToEntityAttribute(Integer code) {
        return code == null ? null : DocumentType.fromCode(code);
    }
}
