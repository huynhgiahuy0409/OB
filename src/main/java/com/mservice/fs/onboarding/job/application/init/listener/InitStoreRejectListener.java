package com.mservice.fs.onboarding.job.application.init.listener;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.StoreRejectFormListener;
import com.mservice.fs.onboarding.job.application.init.task.HandlePendingFormTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
public class InitStoreRejectListener extends StoreRejectFormListener<InitFormRequest, InitFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> onboardingData) {
        TaskData handlePendingFormTask = onboardingData.getTaskData(HandlePendingFormTask.NAME);
        if (Utils.isEmpty(handlePendingFormTask)) {
            Log.MAIN.info("HandlePendingFormTask is null -> can not get application");
            return null;
        }
        return onboardingData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }
}
