package com.mservice.fs.onboarding.job.reasonmessage;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.reasonmessage.listener.PushEventAIListener;
import com.mservice.fs.onboarding.job.reasonmessage.listener.UpdatePackageListener;
import com.mservice.fs.onboarding.job.reasonmessage.task.UpdateReasonMessageTask;
import com.mservice.fs.onboarding.job.reasonmessage.task.ValidateTask;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageRequest;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

@Processor(name = OnboardingProcessor.UPDATE_REASON_MESSAGE)
public class UpdateReasonMessageJob extends OnboardingJob<ReasonMessageRequest, ReasonMessageResponse> {

    public UpdateReasonMessageJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ReasonMessageRequest, ReasonMessageResponse>, ReasonMessageRequest, ReasonMessageResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new ValidateTask(),
                new UpdateReasonMessageTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<ReasonMessageRequest, ReasonMessageResponse>, ReasonMessageRequest, ReasonMessageResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new PushEventAIListener(),
                new UpdatePackageListener()
        );
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
