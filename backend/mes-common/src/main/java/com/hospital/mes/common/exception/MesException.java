package com.hospital.mes.common.exception;
public class MesException extends RuntimeException {
    private final String code;
    public MesException(String code, String message) { super(message); this.code = code; }
    public String code() { return code; }
}
