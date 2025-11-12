package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.utils.Utils;

public class VerifyOtpTrackingStatusListener extends TrackingRequestListener<VerifyOtpRequest, VerifyOtpResponse> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        return jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}
