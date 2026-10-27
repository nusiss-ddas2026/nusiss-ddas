package nusiss.ddas.app.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @ParameterizedTest
    @CsvSource({"1,USER", "2,APPROVER", "3,ADMIN"})
    void fromCodeReturnsMatchingRole(int code, Role expected) {
        assertThat(Role.fromCode(code)).isEqualTo(expected);
    }

    @Test
    void getCodeRoundTripsThroughFromCode() {
        for (Role role : Role.values()) {
            assertThat(Role.fromCode(role.getCode())).isEqualTo(role);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, -1, 99})
    void fromCodeRejectsUnknownCodes(int code) {
        assertThatThrownBy(() -> Role.fromCode(code))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown role code");
    }
}
