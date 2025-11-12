package com.mservice.fs.onboarding.enums;

import com.mservice.fs.model.BackendFailureReason;
import com.mservice.fs.model.BackendStatus;
import com.mservice.fs.model.ErrorCode;

/**
 * @author hoang.thai
 * on 11/27/2023
 */
public enum AIErrorCode implements ErrorCode {

    SUCCESS(Code.SUCCESS, BackendStatus.SUCCESS, "The success response"),
    INTERNAL_SERVER_ERROR(Code.INTERNAL_SERVER_ERROR, "The internal server error"),
    SERVICE_UNAVAILABLE(Code.SERVICE_UNAVAILABLE, "Service unavailable"),
    BAD_REQUEST(Code.BAD_REQUEST, "Bad request"),
    AGE_INVALID(Code.AGE_INVALID, "ID age is invalid"),
    EXPIRED_ID(Code.EXPIRED_ID, "ID is expired"),

    ;

    private int code;
    private String message;
    private BackendStatus status;
    private BackendFailureReason backendFailureReason;

    private AIErrorCode(int code, BackendStatus status, String message, BackendFailureReason backendFailureReason) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.backendFailureReason = backendFailureReason;
    }

    private AIErrorCode(int code, BackendStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    private AIErrorCode(int code, String message) {
        this.code = code;
        this.status = BackendStatus.FAIL;
        this.message = message;
        this.backendFailureReason = BackendFailureReason.EMPTY;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public BackendStatus status() {
        return this.status;
    }

    @Override
    public BackendFailureReason failureReason() {
        return backendFailureReason;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public static final class Code {

        public static final int SUCCESS = 200;
        public static final int QUICK = 202;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int BAD_REQUEST = 404;
        public static final int AGE_INVALID = 2003;
        public static final int EXPIRED_ID = 2004;
    }
}
