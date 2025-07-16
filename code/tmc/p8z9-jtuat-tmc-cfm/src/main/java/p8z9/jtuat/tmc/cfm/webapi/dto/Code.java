package p8z9.jtuat.tmc.cfm.webapi.dto;

public enum Code {
    SUCCESS(0, "成功"),
    FAIL(1, "失败"),
    NOT_FOUND(4, "业务对象不存在"),
    NOT_LOGIN(-100, "身份令牌错误");

    private int code;
    private String message;

    public static Code getByCode(int code) {
        for (Code value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }


    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
