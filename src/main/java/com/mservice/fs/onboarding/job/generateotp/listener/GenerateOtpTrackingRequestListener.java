package com.mservice.fs.onboarding.job.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.utils.Utils;

public class GenerateOtpTrackingRequestListener extends TrackingRequestListener<GenerateOtpRequest, GenerateOtpResponse> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        return jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}
