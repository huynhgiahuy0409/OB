package com.mservice.fs.onboarding.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpResponse extends OnboardingResponse{
    private int currentVerifyTimes;
    private int maxVerifyTimes;
    private int maxGenerateTimes;
    private long validOtpInMillis;
    private String applicationId;

}
