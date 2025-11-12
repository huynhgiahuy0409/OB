package com.mservice.fs.onboarding.job.sign.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.utils.Utils;

public class GenerateOtpTrackingRequestListener extends TrackingRequestListener<GenerateOtpRequest, GenerateOtpResponse> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setApplicationData(applicationData);
        return applicationForm;
    }

    @Override
    protected boolean isActive(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return Utils.isNotEmpty(applicationData);
    }
}
