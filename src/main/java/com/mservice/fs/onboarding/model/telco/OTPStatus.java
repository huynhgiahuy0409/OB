package com.mservice.fs.onboarding.model.telco;

import lombok.Getter;

/**
 * @author phat.duong
 * on 2/25/2025
 **/
@Getter
public enum OTPStatus {

    OTP_SENT_SUCCESS,
    INVALID_PHONE_NUMBER,
    VERIFIED,
    INVALID_OTP,
    NOT_SUPPORTED,
    EXPIRED,
    NOT_VERIFIED,
    OTP_SENT_FAILED,
    PARTNER_ERROR;

}
