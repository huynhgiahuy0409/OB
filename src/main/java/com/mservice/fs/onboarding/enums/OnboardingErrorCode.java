package com.mservice.fs.onboarding.enums;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BackendFailureReason;
import com.mservice.fs.model.BackendStatus;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;

public enum OnboardingErrorCode implements ErrorCode {

    SUCCESS(Code.SUCCESS, "Success"),
    CALL_AI_ERROR(Code.CALL_AI_ERROR, "Call AI error"),
    KNOCK_OUT_RULE_REJECT(Code.KNOCK_OUT_RULE_REJECT, "Knock out rule reject"),
    KNOCK_OUT_RULE_BLOCK(Code.KNOCK_OUT_RULE_BLOCK, "Knock out rule Block"),
    PARTNER_BLOCK(Code.PARTNER_BLOCK, "Partner block"),
    RESPONSE_SOCIAL_SELLER_DATA_INVALID(Code.RESPONSE_SOCIAL_SELLER_DATA_INVALID, "Response from api social seller data invalid"),
    RESPONSE_PAYMENT_INFO_INVALID(Code.RESPONSE_PAYMENT_INFO_INVALID, "Response from api payment info invalid"),
    RESPONSE_FROM_AI_ERROR(Code.RESPONSE_FROM_AI_ERROR, "Response from AI error"),
    CHECK_KYC_FAIL(Code.CHECK_KYC_FAIL, "Check kyc fail"),
    UPDATE_KYC(Code.UPDATE_KYC, "Update kyc"),
    DOB_KYC_NOT_IN_18_TO_50(Code.AGE_NOT_IN_18_TO_50, "Age not in 18 - 50"),
    EXPIRED_DATE_KYC(Code.EXPIRED_DATE_KYC, "Expired date kyc is expired"),
    RE_KYC(Code.RE_KYC, "Re-Kyc"),
    NOT_MAP_BANK(Code.NOT_MAP_BANK, "Not map bank yet"),
    RE_MAP_BANK(Code.RE_MAP_BANK, "Re-Map bank"),
    GET_USER_PROFILE_ERROR(Code.GET_USER_PROFILE_ERROR, "Get User Profile error"),
    PACKAGE_NOT_AVAILABLE(Code.PACKAGE_NOT_AVAILABLE, "Package not available"),
    PACKAGE_NOT_EXIST(Code.PACKAGE_NOT_EXIST, "Package not exist"),
    PACKAGE_NOT_FOUND_FROM_CACHE(Code.PACKAGE_NOT_FOUND_FROM_CACHE, "Package not available"),
    FACE_MATCHING_ERROR(Code.FACE_MATCHING_ERROR, "Error when face matching"),
    FACE_MATCHING_REJECT(Code.FACE_MATCHING_REJECT, "Face matching reject"),
    FACE_MATCHING_TIME_OUT(Code.FACE_MATCHING_TIME_OUT, "Face matching timne out"),
    FACE_MATCHING_ERROR_MISMATCH(Code.FACE_MATCHING_ERROR_MISMATCH, "Face matching call ekyc error"),
    FACE_MATCHING_QA_REJECT(Code.FACE_MATCHING_QA_REJECT, "Face matching QA reject"),
    ONBOARDING_ERROR_CODE(Code.ONBOARDING_ERROR_CODE, "Onboarding error"),
    CONTRACT_NOT_FOUND(Code.CONTRACT_NOT_FOUND, "Contract not found"),
    INVALID_STATUS(Code.INVALID_STATUS, "Invalid Status"),
    FAIL_CHECK_DE_DUP_DIFF_SERVICE(Code.FAIL_CHECK_DE_DUP_DIFF_SERVICE, "Momo check dedup different fail"),
    FAIL_CHECK_DE_DUP_MOMO(Code.FAIL_CHECK_DE_DUP_MOMO, "Momo check dedup fail"),
    FAIL_CHECK_DE_DUP_PARTNER(Code.FAIL_CHECK_DE_DUP_PARTNER, "Partner check dedup fail"),
    PACKAGE_AI_REJECT(Code.PACKAGE_AI_REJECT, "Package Ai response unWhitelist"),
    DEFAULT_ERROR_CODE(Code.DEFAULT_ERROR_CODE, "Unknown ErrorCode"),
    OTP_GENERATE_LIMIT(Code.OTP_GENERATE_LIMIT, "Your response and order have been abandoned because you requested to resend the OTP more than 2 times. Please create a new order and pay again."),
    ADAPTER_RESPONSE_ERROR(Code.ADAPTER_RESPONSE_ERROR, "Adapter ressponse error."),
    OTP_VERIFY_LIMIT(Code.OTP_VERIFY_LIMIT, "Verify reach limit times"),
    CACHE_NOT_FOUND(Code.CACHE_NOT_FOUND, "Cache not found"),
    INVALID_APPLICATION_DATA(Code.INVALID_APPLICATION_DATA, "Invalid application data"),
    OTP_VERIFY_ERROR(Code.OTP_VERIFY_ERROR, "Verify OTP error"),
    INVALID_APPLICATION_ID(Code.INVALID_APPLICATION_ID, "Invalid applicationiD"),
    KYC_ID_CARD_TYPE_NOT_CCCD(Code.KYC_ID_CARD_TYPE_NOT_CCCD, "Id card type not CCCD"),
    VERIFY_FACE_MATCHING(Code.VERIFY_FACE_MATCHING, "Verify face matching"),
    FULL_OCR_FACE_MATCHING(Code.FULL_OCR_FACE_MATCHING, "User have to face matching and ocr"),
    VERIFY_FACE_MATCHING_11(Code.VERIFY_FACE_MATCHING_11, "Verify face matching 11"),
    VERIFY_FACE_MATCHING_1N(Code.VERIFY_FACE_MATCHING_1N, "Verify face matching 1n"),
    NOT_FACE_MATCHING_11(Code.NOT_FACE_MATCHING_11, "Not face matching 11"),
    NOT_FACE_MATCHING_1N(Code.NOT_FACE_MATCHING_1N, "Not face matching 1n"),
    LOAN_DECIDER_REJECT(Code.LOAN_DECIDER_REJECT, "Loan decider reject"),
    GENERATE_CONTRACT_FAIL(Code.GENERATE_CONTRACT_FAIL, "Generate contract fail"),
    STORE_CONTRACT_QUEUE_FAIL(Code.STORE_CONTRACT_QUEUE_FAIL, "Store contract queue fail."),
    GENERATE_LINK_CONTRACT_QUEUE_FAIL(Code.GENERATE_LINK_CONTRACT_QUEUE_FAIL, "Generate link contract queue fail."),
    ACTION_FACE_AND_NOT_FACE_MATCHING(Code.ACTION_FACE_AND_NOT_FACE_MATCHING, "Loan action face and user not face matching"),
    ACTION_FACE_1_N_AND_NOT_FACE_MATCHING(Code.ACTION_FACE_1_N_AND_NOT_FACE_MATCHING, "Loan action face 1-n and user not face matching"),
    ACTION_FACE_MATCHING_AND_FACE_MATCHING(Code.ACTION_FACE_MATCHING_AND_FACE_MATCHING, "Loan action face and user face matching"),
    ACTION_FACE_1_N_AND_FACE_MATCHING(Code.ACTION_FACE_1_N_AND_FACE_MATCHING, "Loan action face 1-n and user face matching"),
    EXCEEDED_LIMIT_FAIL_FACE_MATCHING(Code.EXCEEDED_LIMIT_FAIL_FACE_MATCHING, "You have exceeded the limit of face verification"),
    UPDATE_REASON_MESSAGE_FAIL(Code.UPDATE_REASON_MESSAGE_FAIL, "Update reason message fail"),
    INVALID_REQUEST_UPDATE_REASON_MESSAGE(Code.INVALID_REQUEST_UPDATE_REASON_MESSAGE, "Invalid request update reasonMessage"),
    PACKAGE_IS_NOT_VALID_WITH_VERSION_APP(Code.PACKAGE_IS_NOT_VALID_WITH_VERSION_APP, "Package not valid with version app"),

    ACTION_OTP_TELCO(Code.ACTION_OTP_TELCO, "Require action otp for telco"),
    INVALID_MINI_APP_VERSION(Code.INVALID_MINI_APP_VERSION, "MiniApp version not supported"),
    SEND_OTP_TELCO_FAILED(Code.SEND_OTP_TELCO_FAILED, "Send otp telco failed"),
    INVALID_ACTION_OTP_TELCO(Code.INVALID_ACTION_OTP_TELCO, "Invalid otp telco action"),
    ALERT_SCAM_TEXT(Code.ALERT_SCAM_TEXT, "User need read text alert scam"),
    ALERT_SCAM_VIDEO(Code.ALERT_SCAM_VIDEO, "User need watch video alert scam")


    ;

    private final int code;
    private final String message;
    private final BackendStatus status;
    private final BackendFailureReason backendFailureReason;

    private OnboardingErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
        this.status = BackendStatus.FAIL;
        this.backendFailureReason = BackendFailureReason.SYSTEM_ERROR;
    }

    private OnboardingErrorCode(int code, String message, BackendStatus backendStatus) {
        this.code = code;
        this.message = message;
        this.status = backendStatus;
        this.backendFailureReason = BackendFailureReason.EMPTY;
    }

    private OnboardingErrorCode(int code, BackendStatus backendStatus, String message, BackendFailureReason backendFailureReason) {
        this.code = code;
        this.message = message;
        this.status = backendStatus;
        this.backendFailureReason = backendFailureReason;
    }

    @Override
    public Integer getCode() {
        return code;
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
        return message;
    }

    public static ErrorCode findByCode(int code) {
        for (OnboardingErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        Log.MAIN.error("Can not find error code {}", code);
        return CommonErrorCode.findByCode(code);
    }

    public static class Code {
        public static final int DEFAULT_ERROR_CODE = -1;

        // 0 -> 1000
        public static final int SUCCESS = 0;
        public static final int LOAN_DECIDER_REJECT = 30;
        public static final int CONTRACT_NOT_FOUND = 48;
        public static final int INVALID_STATUS = 49;
        public static final int KNOCK_OUT_RULE_BLOCK = 50;
        public static final int PARTNER_BLOCK = 51;
        public static final int FAIL_CHECK_DE_DUP_DIFF_SERVICE = 51;
        public static final int FAIL_CHECK_DE_DUP_MOMO = 52;
        public static final int FAIL_CHECK_DE_DUP_PARTNER = 53;
        public static final int PACKAGE_AI_REJECT = 54;
        public static final int UPDATE_REASON_MESSAGE_FAIL = 57;
        public static final int INVALID_REQUEST_UPDATE_REASON_MESSAGE = 58;
        public static final int FACE_MATCHING_ERROR = 744;
        public static final int FACE_MATCHING_REJECT = 745;
        public static final int EXCEEDED_LIMIT_FAIL_FACE_MATCHING = 745;
        public static final int FACE_MATCHING_TIME_OUT = 746;
        public static final int FACE_MATCHING_ERROR_MISMATCH = 747;
        public static final int CALL_AI_ERROR = 800;
        public static final int KNOCK_OUT_RULE_REJECT = 801;
        public static final int RESPONSE_SOCIAL_SELLER_DATA_INVALID = 804;
        public static final int RESPONSE_PAYMENT_INFO_INVALID = 805;
        public static final int RESPONSE_FROM_AI_ERROR = 805;
        public static final int FACE_MATCHING_QA_REJECT = 815;

        // 1001 -> 2000
        public static final int ONBOARDING_ERROR_CODE = 1006;
        public static final int OTP_GENERATE_LIMIT = 1101;
        public static final int OTP_VERIFY_ERROR = 1201;
        public static final int OTP_VERIFY_LIMIT = 1202;
        // 2001 -> 3000

        // 3001 -> 4000

        // 4001 -> 5000
        // 411x -> 4120
        public static final int KYC_ID_CARD_TYPE_NOT_CCCD = 4110;
        // 412x -> 4130
        public static final int VERIFY_FACE_MATCHING = 4122;
        public static final int ACTION_FACE_AND_NOT_FACE_MATCHING = 4124;
        public static final int ACTION_FACE_1_N_AND_NOT_FACE_MATCHING = 4125;
        public static final int ACTION_FACE_MATCHING_AND_FACE_MATCHING = 4126;
        public static final int ACTION_FACE_1_N_AND_FACE_MATCHING = 4127;

        // 413x -> 4140
        public static final int NOT_MAP_BANK = 4131;
        public static final int CHECK_KYC_FAIL = 4133;
        public static final int RE_KYC = 4134;
        public static final int UPDATE_KYC = 4135;
        public static final int RE_MAP_BANK = 4135;
        public static final int FULL_OCR_FACE_MATCHING = 4136;
        public static final int AGE_NOT_IN_18_TO_50 = 4137;
        public static final int EXPIRED_DATE_KYC = 4138;


        // 415x -> 4160
        public static final int VERIFY_FACE_MATCHING_11 = 4151;
        public static final int VERIFY_FACE_MATCHING_1N = 4152;

        // 416x -> 4170
        public static final int NOT_FACE_MATCHING_11 = 4161;
        public static final int NOT_FACE_MATCHING_1N = 4162;

        // 5000 -> 6000
        public static final int GET_USER_PROFILE_ERROR = 5000;
        public static final int PACKAGE_NOT_AVAILABLE = 5001;
        public static final int PACKAGE_NOT_FOUND_FROM_CACHE = 5002;
        public static final int PACKAGE_NOT_EXIST = 5003;
        public static final int STORE_CONTRACT_QUEUE_FAIL = 5005;
        public static final int GENERATE_CONTRACT_FAIL = 5006;
        public static final int GENERATE_LINK_CONTRACT_QUEUE_FAIL = 5007;
        public static final int PACKAGE_IS_NOT_VALID_WITH_VERSION_APP = 5008;

        // 9000 -> 10000
        public static final int ADAPTER_RESPONSE_ERROR = 9009;
        public static final int CACHE_NOT_FOUND = 9010;
        public static final int INVALID_APPLICATION_DATA = 9020;
        public static final int INVALID_APPLICATION_ID = 9021;

        public static final int ACTION_OTP_TELCO = 20001;
        public static final int INVALID_MINI_APP_VERSION = 20002;
        public static final int SEND_OTP_TELCO_FAILED = 20003;

        public static final int INVALID_ACTION_OTP_TELCO = 20004;

        public static final int ALERT_SCAM_TEXT = 20011;
        public static final int ALERT_SCAM_VIDEO = 20012;
    }

}
