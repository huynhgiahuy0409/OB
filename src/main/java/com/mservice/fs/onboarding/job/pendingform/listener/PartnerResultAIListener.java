package com.mservice.fs.onboarding.job.pendingform.listener;

import com.mservice.fs.onboarding.job.AbsPartnerResultAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.pendingform.task.ApplicationCacheTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;

public class PartnerResultAIListener extends AbsPartnerResultAIListener<PendingFormRequest, PendingFormResponse> {


    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> onboardingData) {
        return onboardingData.getTaskData(ApplicationCacheTask.NAME).getContent();
    }
}
