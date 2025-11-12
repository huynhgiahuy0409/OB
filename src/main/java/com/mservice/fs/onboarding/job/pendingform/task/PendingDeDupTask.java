package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.init.task.AbsCheckDeDupTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;

/**
 * @author hoang.thai
 * on 1/10/2024
 */
public class PendingDeDupTask extends AbsCheckDeDupTask<PendingFormRequest, PendingFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        return jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
    }
}
