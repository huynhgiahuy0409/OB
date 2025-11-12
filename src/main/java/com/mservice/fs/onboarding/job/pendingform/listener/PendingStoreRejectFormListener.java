package com.mservice.fs.onboarding.job.pendingform.listener;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.StoreRejectFormListener;
import com.mservice.fs.onboarding.job.pendingform.task.ApplicationCacheTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
public class PendingStoreRejectFormListener extends StoreRejectFormListener<PendingFormRequest, PendingFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> onboardingData) {
        TaskData applicationCacheTask = onboardingData.getTaskData(ApplicationCacheTask.NAME);
        if (Utils.isEmpty(applicationCacheTask)) {
            Log.MAIN.info("ApplicationCacheTask is null -> can not get application");
            return null;
        }
        return onboardingData.getTaskData(ApplicationCacheTask.NAME).getContent();
    }
}
