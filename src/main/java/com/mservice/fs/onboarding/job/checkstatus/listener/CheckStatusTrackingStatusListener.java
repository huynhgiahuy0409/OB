package com.mservice.fs.onboarding.job.checkstatus.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.TrackingRequestListener;
import com.mservice.fs.onboarding.job.checkstatus.task.CacheTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ModifyResponseTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.utils.Utils;

public class CheckStatusTrackingStatusListener extends TrackingRequestListener<OnboardingStatusRequest, OnboardingStatusResponse> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        return checkStatusData.getModifyApplicationForm();
    }

    @Override
    protected boolean isActive(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        return Utils.isNotEmpty(checkStatusData) && Utils.isNotEmpty(checkStatusData.getModifyApplicationForm());
    }
}
