package com.mservice.fs.onboarding.job;

import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;

public class TrackingPartnerStatusListener<T extends UpdatingStatusRequest, R extends UpdatingStatusResponse> extends TrackingRequestListener<T,R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) {
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setApplicationData(jobData.getTaskData(UpdatingStatusTask.NAME).getContent());
        return applicationForm;
    }

    @Override
    protected boolean isActive(OnboardingData<T, R> jobData) {
        return jobData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode());
    }
}
