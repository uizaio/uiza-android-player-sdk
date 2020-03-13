package com.uiza.sdk.exceptions;

public class UZException extends Exception {

    private int code;

    public UZException() {
        super();
    }

    public UZException(int code, String message) {
        super(message);
        this.code = code;
    }

    public UZException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public UZException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
