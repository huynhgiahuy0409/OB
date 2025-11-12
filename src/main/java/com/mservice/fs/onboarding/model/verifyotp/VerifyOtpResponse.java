package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.OtpResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyOtpResponse extends OtpResponse {
    private int remainingVerifyTimes;
    private ApplicationData applicationData;
}
