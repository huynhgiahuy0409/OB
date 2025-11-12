package com.mservice.fs.onboarding.job.telco;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpRequest;
import com.mservice.fs.onboarding.model.OtpResponse;
import com.mservice.fs.utils.Utils;

public class OtpTrackingRequestListener<T extends OtpRequest, R extends OtpResponse> extends TrackingRequestListener<T, R> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(GetApplicationTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<T, R> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}