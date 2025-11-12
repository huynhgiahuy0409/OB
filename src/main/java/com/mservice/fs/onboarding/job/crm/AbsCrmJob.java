package com.mservice.fs.onboarding.job.crm;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.job.PlatformJob;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.crm.task.*;
import com.mservice.fs.onboarding.model.crm.CrmRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsCrmJob<T extends CrmRequest> extends PlatformJob<PlatformData<T,CrmResponse>, T,CrmResponse, OnboardingConfig> {

    protected AbsCrmJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<PlatformData<T, CrmResponse>, T, CrmResponse, OnboardingConfig>> getTaskList() throws Exception {
        List<Task<PlatformData<T, CrmResponse>, T, CrmResponse, OnboardingConfig>> tasks = new ArrayList<>();
        tasks.add(new LoadCrmConfigTask<>());
        tasks.add(new CrmGetUserProfileTask<>());
        tasks.add(getCacheTask());
        tasks.add(new GetDBTask<>());
        List<Task<PlatformData<T, CrmResponse>, T, CrmResponse, OnboardingConfig>> serviceTasks = getServiceTask();
        if (Utils.isNotEmpty(serviceTasks)) {
            tasks.addAll(serviceTasks);
        }
        tasks.add(new BuildResponseTask<>());
        return tasks;
    }

    protected Task<PlatformData<T, CrmResponse>,T, CrmResponse, OnboardingConfig> getCacheTask() {
        return new GetCacheTask<>();
    }

    protected abstract List<Task<PlatformData<T, CrmResponse>, T, CrmResponse, OnboardingConfig>> getServiceTask();


    @Override
    protected List<AbstractListener<PlatformData<T, CrmResponse>, T, CrmResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of();
    }


    @Override
    public ErrorCode getSystemBugErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }

    @Override
    public ErrorCode getInvalidRequestErrorCode() {
        return CommonErrorCode.INVALID_REQUEST;
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.JOB_TIMEOUT;
    }

    @Override
    public ErrorCode getNoResponseErrorCode() {
        return CommonErrorCode.NO_RESPONSE;
    }

    @Override
    protected boolean executiveWhenTimeout() {
        return false;
    }
}
