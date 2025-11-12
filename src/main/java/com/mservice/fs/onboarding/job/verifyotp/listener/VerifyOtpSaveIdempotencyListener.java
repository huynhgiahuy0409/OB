package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.SaveCacheListener;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;

/**
 * @author phat.duong
 * on 1/15/2025
 **/
public class VerifyOtpSaveIdempotencyListener extends SaveCacheListener<VerifyOtpRequest, VerifyOtpResponse> {
    @Override
    protected String getKey(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        return onboardingData.getProcessName() + ":" + String.join("_", onboardingData.getServiceId(), onboardingData.getInitiatorId(), onboardingData.getRequest().getApplicationId());
    }
}
