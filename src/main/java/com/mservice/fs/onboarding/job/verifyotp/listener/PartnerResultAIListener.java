package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.onboarding.job.AbsPartnerResultAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;

public class PartnerResultAIListener<T extends OnboardingRequest, R extends VerifyOtpResponse> extends AbsPartnerResultAIListener<T, R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> onboardingData) {
        return onboardingData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
    }
}
