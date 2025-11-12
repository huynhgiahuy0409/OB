package com.mservice.fs.onboarding.job.application.init.listener;

import com.mservice.fs.onboarding.job.AbsPartnerResultAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.init.task.HandlePendingFormTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;

public class PartnerResultAIListener extends AbsPartnerResultAIListener<InitFormRequest, InitFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> onboardingData) {
        return onboardingData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }
}
