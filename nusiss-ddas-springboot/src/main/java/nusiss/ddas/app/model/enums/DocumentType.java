package nusiss.ddas.app.model.enums;

public enum DocumentType {
    LEAVE(1),
    PROCUREMENT(2),
    MEDICAL(3);

    private final int code;

    DocumentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DocumentType fromCode(int code) {
        for (DocumentType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type code: " + code);
    }
}
