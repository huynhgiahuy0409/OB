package com.mservice.fs.onboarding.job.offer;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.LendingConfiguredPackageTask;
import com.mservice.fs.onboarding.job.offer.listener.UpdatePackageCacheListener;
import com.mservice.fs.onboarding.job.offer.task.CacheTask;
import com.mservice.fs.onboarding.job.offer.task.GetOfferKnockOutRuleTask;
import com.mservice.fs.onboarding.job.offer.task.GetPackageTask;
import com.mservice.fs.onboarding.job.offer.task.ModifyResponseTask;
import com.mservice.fs.onboarding.job.offer.task.ServiceInfoTask;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
@Processor(name = OnboardingProcessor.GET_OFFERS)
public class GetOfferJob extends OnboardingJob<OfferRequest, OfferResponse> {

    public GetOfferJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<OfferRequest, OfferResponse>, OfferRequest, OfferResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new ServiceInfoTask(),
                new CacheTask(),
                new AbsGetUserProfileTask<>(),
                new GetOfferKnockOutRuleTask(),
                new GetPackageTask(),
                new LendingConfiguredPackageTask<>(),
                new ModifyResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<OfferRequest, OfferResponse>, OfferRequest, OfferResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdatePackageCacheListener()
        );
    }
}
