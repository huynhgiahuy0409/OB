package com.mservice.fs.onboarding.model.generateotp;

import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.OtpResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenerateOtpResponse extends OtpResponse {
    private int currentGenerateTimes;
}
