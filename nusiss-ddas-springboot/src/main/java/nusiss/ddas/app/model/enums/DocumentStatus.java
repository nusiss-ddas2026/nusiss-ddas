package nusiss.ddas.app.model.enums;

public enum DocumentStatus {
    PENDING(1),
    APPROVED(2),
    REJECTED(3),
    REVIEWING(4);

    private final int code;

    DocumentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DocumentStatus fromCode(int code) {
        for (DocumentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown document status code: " + code);
    }
}
