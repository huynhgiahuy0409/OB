package com.mservice.fs.onboarding.job.application.submit;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.submit.task.CreateDataTestTask;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
//@Processor(name = "create-submit")
public class CreateDataJob extends OnboardingJob<SubmitRequest, SubmitResponse> {

    public CreateDataJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
//                new GetUserProfileTask<>(),
//                new GetCacheApplicationTask(),
//                new StoreApplicationTask(),
                //Check Knock out rule Task
//                new CheckLoanDeciderTask()
                new CreateDataTestTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of();
    }
}
