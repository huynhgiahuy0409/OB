package com.mservice.fs.onboarding.job.application.partner;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.partner.listener.AddCacheListener;
import com.mservice.fs.onboarding.job.application.partner.task.ApplicationGetUserProfileTask;
import com.mservice.fs.onboarding.job.application.partner.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.application.partner.task.GetCacheTask;
import com.mservice.fs.onboarding.job.application.partner.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
@Processor(name = {OnboardingProcessor.GET_APPLICATION_PARTNER, OnboardingProcessor.GET_APPLICATION_EXISTING})
public class GetApplicationPartnerJob extends OnboardingJob<ApplicationPartnerRequest, ApplicationPartnerResponse> {
    public GetApplicationPartnerJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse>, ApplicationPartnerRequest, ApplicationPartnerResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new GetCacheTask(),
                new GetApplicationTask(),
                new ApplicationGetUserProfileTask(),
                new ModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse>, ApplicationPartnerRequest, ApplicationPartnerResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(new AddCacheListener());
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
