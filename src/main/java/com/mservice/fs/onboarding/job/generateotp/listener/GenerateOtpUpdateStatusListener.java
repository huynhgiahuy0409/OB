package com.mservice.fs.onboarding.job.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.UpdateApplicationStatusListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.utils.Utils;

import java.util.Optional;

public class GenerateOtpUpdateStatusListener extends UpdateApplicationStatusListener<GenerateOtpRequest, GenerateOtpResponse> {

    @Override
    protected String getApplicationId(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        return applicationData.getApplicationId();
    }

    @Override
    protected ApplicationStatus getStatus(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        return applicationData.getStatus();
    }

    @Override
    protected boolean isActive(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }

    @Override
    protected int getReasonId(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return applicationForm.getApplicationData().getReasonId();
    }

    @Override
    protected String getReasonMessage(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return applicationForm.getApplicationData().getReasonMessage();
    }
}
