package com.mservice.fs.onboarding.job.sign.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.UpdateApplicationStatusListener;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.utils.Utils;

public class GenerateOtpUpdateStatusListener extends UpdateApplicationStatusListener<GenerateOtpRequest, GenerateOtpResponse> {

    @Override
    protected String getApplicationId(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return applicationData.getApplicationId();
    }

    @Override
    protected ApplicationStatus getStatus(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return applicationData.getStatus();
    }

    @Override
    protected boolean isActive(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return Utils.isNotEmpty(applicationData);
    }

    @Override
    protected int getReasonId(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return applicationData.getReasonId();
    }

    @Override
    protected String getReasonMessage(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return applicationData.getReasonMessage();
    }
}
