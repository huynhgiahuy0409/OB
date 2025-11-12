package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.SendApplicationPlatformTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;

/**
 * @author phat.duong
 * on 3/10/2025
 **/
public class SubmitSendPlatformTask extends SendApplicationPlatformTask<SubmitRequest, SubmitResponse> {

    @Override
    protected ApplicationData getApplicationData(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        ApplicationForm applicationForm = onboardingData.getTaskData(ApplicationTask.NAME).getContent();
        return applicationForm.getApplicationData();
    }

    @Override
    protected String getProcessName(OnboardingData<SubmitRequest, SubmitResponse> platformData) {
        return "store-application";
    }
}
