package com.mservice.fs.onboarding.job.application.step;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.step.listener.FillFormTrackingRequestListener;
import com.mservice.fs.onboarding.job.application.step.listener.UpdateCacheListener;
import com.mservice.fs.onboarding.job.application.step.task.GetCacheDataTask;
import com.mservice.fs.onboarding.job.application.step.task.UpdateApplicationDataTask;
import com.mservice.fs.onboarding.model.application.fillform.FillFormRequest;
import com.mservice.fs.onboarding.model.application.fillform.FillFormResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/13/2023
 */

@Processor(name = {OnboardingProcessor.FIRST_SUBMIT, OnboardingProcessor.SECOND_SUBMIT})
public class FillFormJob extends OnboardingJob<FillFormRequest, FillFormResponse> {

    public FillFormJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<FillFormRequest, FillFormResponse>, FillFormRequest, FillFormResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new GetCacheDataTask(),
                new UpdateApplicationDataTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<FillFormRequest, FillFormResponse>, FillFormRequest, FillFormResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener(),
                new FillFormTrackingRequestListener()
        );
    }
}
