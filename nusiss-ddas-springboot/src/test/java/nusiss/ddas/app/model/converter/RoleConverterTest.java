package nusiss.ddas.app.model.converter;

import org.junit.jupiter.api.Test;

import nusiss.ddas.app.model.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleConverterTest {

    private final RoleConverter converter = new RoleConverter();

    @Test
    void convertsRoleToItsDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(Role.USER)).isEqualTo(1);
        assertThat(converter.convertToDatabaseColumn(Role.APPROVER)).isEqualTo(2);
        assertThat(converter.convertToDatabaseColumn(Role.ADMIN)).isEqualTo(3);
    }

    @Test
    void convertsDatabaseCodeBackToRole() {
        assertThat(converter.convertToEntityAttribute(1)).isEqualTo(Role.USER);
        assertThat(converter.convertToEntityAttribute(2)).isEqualTo(Role.APPROVER);
        assertThat(converter.convertToEntityAttribute(3)).isEqualTo(Role.ADMIN);
    }

    @Test
    void handlesNullsOnBothSides() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void rejectsUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(42))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
