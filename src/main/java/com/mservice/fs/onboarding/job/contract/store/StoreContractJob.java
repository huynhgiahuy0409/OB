package com.mservice.fs.onboarding.job.contract.store;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.contract.store.listener.StoreDBListener;
import com.mservice.fs.onboarding.job.contract.store.task.GenerateContractTask;
import com.mservice.fs.onboarding.job.contract.store.task.ModifiedResponse;
import com.mservice.fs.onboarding.job.contract.store.task.SendContractAdapterTask;
import com.mservice.fs.onboarding.job.contract.store.task.StoreContractQueueTask;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

@Processor(name = OnboardingProcessor.CONTRACT)
public class StoreContractJob extends OnboardingJob<StoreContractRequest, StoreContractResponse> {


    public StoreContractJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<StoreContractRequest, StoreContractResponse>, StoreContractRequest, StoreContractResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new GenerateContractTask(),
                new StoreContractQueueTask(),
                new SendContractAdapterTask(),
                new ModifiedResponse()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<StoreContractRequest, StoreContractResponse>, StoreContractRequest, StoreContractResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new StoreDBListener()
        );
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
