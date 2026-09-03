package nusiss.ddas.app.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nusiss.ddas.app.model.enums.Role;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Role role) {
        return role == null ? null : role.getCode();
    }

    @Override
    public Role convertToEntityAttribute(Integer code) {
        return code == null ? null : Role.fromCode(code);
    }
}
