package com.mservice.fs.onboarding.job.application.info;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.info.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.application.info.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.Collections;
import java.util.List;

/**
 * @author phat.duong
 * on 10/10/2024
 **/
@Processor(name = OnboardingProcessor.GET_APPLICATION_DISBURSE)
public class GetApplicationDisburseJob extends OnboardingJob<ApplicationRequest, ApplicationResponse> {

    public GetApplicationDisburseJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ApplicationRequest, ApplicationResponse>, ApplicationRequest, ApplicationResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new GetApplicationTask(),
                new ModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<ApplicationRequest, ApplicationResponse>, ApplicationRequest, ApplicationResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return Collections.emptyList();
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
