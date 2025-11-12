package com.mservice.fs.onboarding.job.checkwhitelistpackage;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusQuickPackageTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ServiceObInfoTask;
import com.mservice.fs.onboarding.job.SaveCacheListener;
import com.mservice.fs.onboarding.job.checkwhitelistpackage.task.BuildResponseTask;
import com.mservice.fs.onboarding.job.IdempotencyTask;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.Arrays;
import java.util.List;

/**
 * @author phat.duong
 * on 12/20/2024
 **/

@Processor(name = OnboardingProcessor.CHECK_WHITE_LIST_PACKAGE)
public class CheckWhiteListPackageJob extends OnboardingJob<OnboardingStatusRequest, OnboardingStatusResponse> {

    public CheckWhiteListPackageJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse>, OnboardingStatusRequest, OnboardingStatusResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new IdempotencyTask<>(),
                new LoadDataTask<>(true),
                new ServiceObInfoTask<>(),
                new AbsGetUserProfileTask<>(),
                new CheckStatusQuickPackageTask(),
                new BuildResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse>, OnboardingStatusRequest, OnboardingStatusResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(new SaveCacheListener<>());
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
