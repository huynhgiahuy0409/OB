package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.utils.Utils;

public class SumitFormTrackingRequestListener<T extends OnboardingRequest, R extends OnboardingResponse> extends TrackingRequestListener<T, R> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(ApplicationTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<T, R> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}
