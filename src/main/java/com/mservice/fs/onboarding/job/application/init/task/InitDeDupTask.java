package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;

/**
 * @author hoang.thai
 * on 1/10/2024
 */
public class InitDeDupTask extends AbsCheckDeDupTask<InitFormRequest, InitFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        return jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }
}
