package com.mservice.fs.onboarding.job.application.step.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.application.step.task.UpdateApplicationDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.fillform.FillFormRequest;
import com.mservice.fs.onboarding.model.application.fillform.FillFormResponse;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public class FillFormTrackingRequestListener extends TrackingRequestListener<FillFormRequest, FillFormResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<FillFormRequest, FillFormResponse> jobData) {
        return jobData.getTaskData(UpdateApplicationDataTask.NAME).getContent();
    }

    @Override
    protected boolean isActive(OnboardingData<FillFormRequest, FillFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(UpdateApplicationDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }
}
