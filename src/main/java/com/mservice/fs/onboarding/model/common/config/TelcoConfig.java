package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TelcoConfig {
    private String serviceId;
    private String sourceId;
    private int maxGenerateOtpTimes;
    private int maxVerifyOtpTimes;
    private long resetOtpInMillis;
    private int validOtpInMillis;
}
