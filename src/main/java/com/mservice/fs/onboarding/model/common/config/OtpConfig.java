package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/11/2023
 */
@Getter
@Setter
public class OtpConfig {

    private String serviceId;
    private String partnerId;
    private int maxGenerateOtpTimes;
    private int maxVerifyOtpTimes;
    private long resetOtpInMillis;
    private int validOtpInMillis;
    private Integer otpLength;

}
