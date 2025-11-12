package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.onboarding.job.AbsPartnerResultAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;

public class PartnerResultLogListener<T extends ConfirmRequest, R extends OnboardingResponse> extends AbsPartnerResultAIListener<T,R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> onboardingData) {
        return onboardingData.getTaskData(ApplicationTask.NAME).getContent();
    }
}
