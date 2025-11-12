package com.mservice.fs.onboarding.job.application.info;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.info.listener.UpdateLinkContractListener;
import com.mservice.fs.onboarding.job.application.info.task.GenFileContractLinkTask;
import com.mservice.fs.onboarding.job.application.info.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.application.info.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

@Processor(name = OnboardingProcessor.GET_APPLICATION)
public class GetApplicationJob extends OnboardingJob<ApplicationRequest, ApplicationResponse> {

    public GetApplicationJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ApplicationRequest, ApplicationResponse>, ApplicationRequest, ApplicationResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new GetApplicationTask(),
                new GenFileContractLinkTask(),
                new ModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<ApplicationRequest, ApplicationResponse>, ApplicationRequest, ApplicationResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateLinkContractListener()
        );
    }


    @Override
    protected boolean parseResultMessage() {
        return false;
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<ApplicationRequest, ApplicationResponse> data, ApplicationResponse response) {
        // NOOP
    }
}
