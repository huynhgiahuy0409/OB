package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.onboarding.job.AbsNotiUserListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;

public class NotiUserListener extends AbsNotiUserListener<VerifyOtpRequest, VerifyOtpResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        return jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
    }
}
