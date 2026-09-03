package nusiss.ddas.app.model.enums;

public enum Role {
    USER(1),
    APPROVER(2),
    ADMIN(3);

    private final int code;

    Role(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Role fromCode(int code) {
        for (Role role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
