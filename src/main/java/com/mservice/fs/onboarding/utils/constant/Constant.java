package com.mservice.fs.onboarding.utils.constant;

import com.mservice.fs.onboarding.model.ApplicationStatus;

import java.util.Set;

/**
 * @author hoang.thai
 * on 10/30/2023
 */
public class Constant {

    public static final String ALL_LENDER = "ALL_LENDER";
    public static final int NUMBER_MAP_BANK = 1000100000;
    public static final String KHONG_THOI_HAN = "KHÔNG THỜI HẠN";
    public static final String APPLICATION_ID_KEY = "applicationId";
    public static final String MINI_APP_TRACK_VER = "miniapp-track-ver";
    public static final String MOMO_SESSION_KEY = "momo-session-key-tracking";
    public static final Set<ApplicationStatus> STORE_REJECTED_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);
    public static final Set<ApplicationStatus> PARTNER_REMOVED_CACHE_STATUS = Set.of(ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO, ApplicationStatus.REJECTED_BY_LENDER);
    public static final Set<ApplicationStatus> REVERT_SIGN_STATUS = Set.of(ApplicationStatus.GENERATED_OTP_SIGN, ApplicationStatus.VERIFIED_OTP_SIGN_FAILED, ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_SIGN_EXCEED, ApplicationStatus.REJECTED_BY_LIMITED_GENERATED_OTP_SIGN_EXCEED);

    public static final String APPLICATION_DATA = "applicationData";
    public static final int AGE_18 = 18;
    public static final int AGE_50 = 50;
    public static final String STRING_JOIN_NAME_AND_TYPE_CONTRACT = "@";
    public static final String PARTNER_ID_KEY = "partnerId";
    public static final String EXTRA_DATA_FIELD_KEY = "extraData";
    public static final String IS_GO_NFC_FIELD_KEY = "isGoNFC";

    public static final String COMMA_DELIMITER = ",";

    public static final String P_LOAN_ACTION_AI_CONFIG = "P_LOAN_ACTION_AI_CONFIG";
    public static final String P_SERVICE_CONFIG = "P_SERVICE_CONFIG";
    public static final String P_AI_RULE = "P_AI_RULE";
    public static final String P_ACTION_CONFIG = "P_ACTION_CONFIG";
    public static final String P_ACTION_FIELD = "P_ACTION_FIELD";
    public static final String P_OTP_CONFIG = "P_OTP_CONFIG";
    public static final String P_FLOW_CONFIG = "P_FLOW_CONFIG";
    public static final String P_RENDER_DATA = "P_RENDER_DATA";
    public static final String P_FORMULA_DUE_DATE_CONFIG = "P_FORMULA_DUE_DATE_CONFIG";
    public static final String P_SERVICE_GROUP = "P_SERVICE_GROUP";
    public static final String P_CONTRACT_CONFIG = "P_CONTRACT_CONFIG";
    public static final String P_AI_MAPPING_CONFIG = "P_AI_MAPPING_CONFIG";
    public static final String P_CONSENT_CONFIG = "P_CONSENT_CONFIG";
    public static final String P_CRM_CONFIG = "P_CRM_CONFIG";
    public static final String TIME_REMIND_USER = "TIME_REMIND_USER";


    public static final String SERVICE_ID_COLUMN = "SERVICE_ID";
    public static final String USER_TYPE_KNOCK_OUT_RULE_COLUMN = "USER_TYPE_KNOCK_OUT_RULE";
    public static final String USER_TYPE_GET_PACKAGE_COLUMN = "USER_TYPE_GET_PACKAGE";
    public static final String PENDING_FORM_CACHE_TIME_IN_MILLIS_COLUMN = "PENDING_FORM_CACHE_TIME_IN_MILLIS";
    public static final String BANNED_FORM_CACHE_TIME_IN_MILLIS_COLUMN = "BANNED_FORM_CACHE_TIME_IN_MILLIS";
    public static final String APPLICATION_STATUS_HIT_DEDUP_COLUMN = "APPLICATION_STATUS_HIT_DEDUP";
    public static final String STATUS_ALLOW_CANCEL_BY_LENDER = "STATUS_ALLOW_CANCEL_BY_LENDER";
    public static final String STATUS_ALLOW_REJECT_BY_LENDER = "STATUS_ALLOW_REJECT_BY_LENDER";

    public static final String ACTION_TYPE_COLUMN = "ACTION_TYPE";
    public static final String DELETED_CACHE_WHEN_REJECT_COLUMN = "DELETED_CACHE_WHEN_REJECT";
    public static final String DELETE_CACHE_OUT_WHITE_LIST = "DELETE_CACHE_OUT_WHITE_LIST";
    public static final String IS_GENERATE_OTP_WHEN_SUBMIT = "IS_GENERATE_OTP_WHEN_SUBMIT";
    public static final String IS_CALCULATE_DUE_DATE_COLUMN = "IS_CALCULATE_DUE_DATE";
    public static final String LOAN_GOAL_DEFAULT = "LOAN_GOAL_DEFAULT";
    public static final String SERVICE_NAME_COLUMN = "SERVICE_NAME";
    public static final String AI_ACTION_MAPPING_COLUMN = "AI_ACTION_MAPPING";
    public static final String AI_ACTION_MAPPING_LD_COLUMN = "AI_ACTION_MAPPING_LD";
    public static final String ACTION_ID_COLUMN = "ACTION_ID";
    public static final String PROCESS_NAME_COLUMN = "PROCESS_NAME";
    public static final String FIELD_NAME_COLUMN = "FIELD_NAME";
    public static final String RENDER_TYPE_COLUMN = "RENDER_TYPE";
    public static final String TITLE_COLUMN = "TITLE";
    public static final String MESSAGE_COLUMN = "MESSAGE";
    public static final String BUTTON_DIRECTION_COLUMN = "BUTTON_DIRECTION";
    public static final String IMAGE_COLUMN = "IMAGE";
    public static final String PRIMARY_COLUMN = "PRIMARY";
    public static final String SECONDARY_COLUMN = "SECONDARY";
    public static final String TRACKING_PARAMS_COLUMN = "TRACKING_PARAMS";
    public static final String NAVIGATION_TYPE_COLUMN = "NAVIGATION_TYPE";
    public static final String RESULT_CODE_COLUMN = "RESULT_CODE";
    public static final String USER_PROFILE_INFO_COLUMN = "USER_PROFILE_INFO";
    public static final String AI_LOAN_ACTION_NAME_COLUMN = "AI_LOAN_ACTION";
    public static final String REDIRECT_PROCESS_NAME_COLUMN = "REDIRECT_PROCESS_NAME";
    public static final String TAG_NAME = "actionKor";
    public static final String LOAN_DECIDER_TAG_NAME = "actionLoanDecider";

    //crm
    public static final String UNKNOWN_VALUE = "UNKNOWN_VALUE";
    public static final String ONBOARDING_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String CACHE_TYPE_VALUE = "CACHE";
    public static final String DATABASE_TYPE_VALUE = "DATABASE";
    public static final String CACHE_AND_DATABASE_TYPE_VALUE = "CACHE_AND_DATABASE";
    public static final String PACKAGE_TAG_NAME = "packageLenderName";
    public static final String CALLER_ID = "caller_id";
    public static final String CONTRACT_LENGTH = "CONTRACT_LENGTH";
    public static final String ZERO_INTEREST = "ZERO_INTEREST";

    public static final String ANY_ACTION = "ANY_ACTION";
    public static final String SCORE_SERVICE_ID_RISK = "SCORE_SERVICE_ID_RISK";
    public static final String SCORE_MSG_TYPE_RISK = "SCORE_MSG_TYPE_RISK";
    public static final String ONBOARDING = "ONBOARDING";
    public static final String RECHECK_PENDING_FORM = "RECHECK_PENDING_FORM";
    public static final String REAPPLY_WHEN_LENDER_REJECT = "REAPPLY_WHEN_LENDER_REJECT";


    public static final String SERVICE_MERGE = "SERVICE_MERGE";
    //db
    public static final String FULL_NAME = "FULL_NAME";
    public static final String TAX_CODE = "TAX_CODE";
    public static final String PRODUCT_NAME = "PRODUCT_NAME";
    public static final String OPTION = "option";
    public static final String NFC_OPTION = "nfcOption";
    public static final String EXTRA_DATA = "extraData";

    /* UserProfileInfo field */
    public static final String DG2_FIELD = "dg2";
}
