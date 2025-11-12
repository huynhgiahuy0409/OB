package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.init.task.UpdateProfilePendingFormTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public class PendingUpdateProfileTask extends UpdateProfilePendingFormTask<PendingFormRequest, PendingFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> onboardingData) {
        return onboardingData.getTaskData(ApplicationCacheTask.NAME).getContent();
    }

    @Override
    protected UserProfileInfo getUserProfile(OnboardingData<PendingFormRequest, PendingFormResponse> onboardingData) {
        return onboardingData.getTaskData(PendingFormGetUserProfileTask.NAME).getContent();
    }

}
