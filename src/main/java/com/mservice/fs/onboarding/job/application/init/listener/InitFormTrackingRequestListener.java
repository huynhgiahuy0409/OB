package com.mservice.fs.onboarding.job.application.init.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.application.init.task.HandlePendingFormTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.utils.Utils;

public class InitFormTrackingRequestListener extends TrackingRequestListener<InitFormRequest, InitFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        return jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}
