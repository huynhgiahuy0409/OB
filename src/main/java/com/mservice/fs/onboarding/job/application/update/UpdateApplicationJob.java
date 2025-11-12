package com.mservice.fs.onboarding.job.application.update;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.AbsPartnerResultAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.update.listener.PushEventAIListener;
import com.mservice.fs.onboarding.job.application.update.task.UpdateApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.update.UpdateApplicationRequest;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

@Processor(name = OnboardingProcessor.UPDATE_APPLICATION)
public class UpdateApplicationJob extends OnboardingJob<UpdateApplicationRequest, OnboardingResponse> {

    public UpdateApplicationJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<UpdateApplicationRequest, OnboardingResponse>, UpdateApplicationRequest, OnboardingResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new UpdateApplicationTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<UpdateApplicationRequest, OnboardingResponse>, UpdateApplicationRequest, OnboardingResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new PushEventAIListener()
        );
    }


    @Override
    protected boolean parseResultMessage() {
        return false;
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<UpdateApplicationRequest, OnboardingResponse> data, OnboardingResponse response) {
        // NOOP
    }
}
