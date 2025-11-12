package com.mservice.fs.onboarding.job.active;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.active.task.ActivateTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.lock.ActiveRequest;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

//@Processor(name = OnboardingProcessor.ACTIVE)
public class ActivateApplicationJob extends OnboardingJob<ActiveRequest, OnboardingResponse> {

    public ActivateApplicationJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ActiveRequest, OnboardingResponse>, ActiveRequest, OnboardingResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(new ActivateTask());
    }

    @Override
    protected List<AbstractListener<OnboardingData<ActiveRequest, OnboardingResponse>, ActiveRequest, OnboardingResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of();
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<ActiveRequest, OnboardingResponse> data, OnboardingResponse response) {

    }
}
