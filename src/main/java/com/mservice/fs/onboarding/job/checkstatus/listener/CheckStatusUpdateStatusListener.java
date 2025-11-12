package com.mservice.fs.onboarding.job.checkstatus.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.UpdateApplicationStatusListener;
import com.mservice.fs.onboarding.job.checkstatus.task.CacheTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ModifyResponseTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.utils.Utils;

public class CheckStatusUpdateStatusListener extends UpdateApplicationStatusListener<OnboardingStatusRequest, OnboardingStatusResponse> {

    @Override
    protected String getApplicationId(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        return checkStatusData.getModifyApplicationForm().getApplicationData().getApplicationId();
    }

    @Override
    protected ApplicationStatus getStatus(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        return checkStatusData.getModifyApplicationForm().getApplicationData().getStatus();
    }

    @Override
    protected boolean isActive(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        return Utils.isNotEmpty(checkStatusData)
                && Utils.isNotEmpty(checkStatusData.getModifyApplicationForm())
                && Utils.isNotEmpty(checkStatusData.getModifyApplicationForm().getApplicationData());
    }
}
