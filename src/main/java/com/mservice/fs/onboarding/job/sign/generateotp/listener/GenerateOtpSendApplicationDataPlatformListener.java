package com.mservice.fs.onboarding.job.sign.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.SendApplicationDataPlatformListener;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;

public class GenerateOtpSendApplicationDataPlatformListener extends SendApplicationDataPlatformListener<GenerateOtpRequest, GenerateOtpResponse> {
    @Override
    protected ApplicationData getApplicationData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws Exception {
        return jobData.getTaskData(GetApplicationTask.NAME).getContent();
    }
}
