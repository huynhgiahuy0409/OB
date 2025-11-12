package com.mservice.fs.onboarding.job.crm;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.crm.listener.InsertTrackingListener;
import com.mservice.fs.onboarding.job.crm.task.RemoveCacheTask;
import com.mservice.fs.onboarding.model.crm.CrmCancelApplicationRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;


@Processor(name = "cancel-application")
public class CrmCancelJob extends AbsCrmJob<CrmCancelApplicationRequest> {

    public CrmCancelJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<PlatformData<CrmCancelApplicationRequest, CrmResponse>, CrmCancelApplicationRequest, CrmResponse, OnboardingConfig>> getServiceTask() {
        return null;
    }

    @Override
    protected Task<PlatformData<CrmCancelApplicationRequest, CrmResponse>, CrmCancelApplicationRequest, CrmResponse, OnboardingConfig> getCacheTask() {
        return new RemoveCacheTask();
    }

    @Override
    protected List<AbstractListener<PlatformData<CrmCancelApplicationRequest, CrmResponse>, CrmCancelApplicationRequest, CrmResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new InsertTrackingListener()
        );
    }
}
