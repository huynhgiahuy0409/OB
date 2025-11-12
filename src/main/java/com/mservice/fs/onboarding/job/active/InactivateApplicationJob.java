package com.mservice.fs.onboarding.job.active;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.active.task.InactiveApplicationTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.lock.InactiveRequest;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

//@Processor(name = OnboardingProcessor.INACTIVE)
public class InactivateApplicationJob extends OnboardingJob<InactiveRequest, OnboardingResponse> {
    public InactivateApplicationJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<InactiveRequest, OnboardingResponse>, InactiveRequest, OnboardingResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(new InactiveApplicationTask());
    }

    @Override
    protected List<AbstractListener<OnboardingData<InactiveRequest, OnboardingResponse>, InactiveRequest, OnboardingResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of();
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<InactiveRequest, OnboardingResponse> data, OnboardingResponse response) {

    }
}
