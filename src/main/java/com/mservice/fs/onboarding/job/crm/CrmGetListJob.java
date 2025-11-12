package com.mservice.fs.onboarding.job.crm;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.crm.CrmListRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.processor.Task;

import java.util.List;


@Processor(name = "get-list-application")
public class CrmGetListJob extends AbsCrmJob<CrmListRequest> {

    public CrmGetListJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<PlatformData<CrmListRequest, CrmResponse>, CrmListRequest, CrmResponse, OnboardingConfig>> getServiceTask() {
        return null;
    }


}
