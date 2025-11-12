package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.onboarding.job.AbsNotiUserListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;

public class NotiUserListener<T extends OnboardingRequest, R extends SubmitResponse> extends AbsNotiUserListener<T,R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(ApplicationTask.NAME).getContent();
    }

}
