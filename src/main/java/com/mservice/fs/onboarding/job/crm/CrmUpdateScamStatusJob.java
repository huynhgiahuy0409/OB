package com.mservice.fs.onboarding.job.crm;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Processor;
import com.mservice.fs.job.PlatformJob;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.crm.listener.InsertTrackingUpdateListener;
import com.mservice.fs.onboarding.job.crm.task.CrmGetUserProfileTask;
import com.mservice.fs.onboarding.job.crm.task.GetCacheTask;
import com.mservice.fs.onboarding.job.crm.task.LoadCrmConfigTask;
import com.mservice.fs.onboarding.job.crm.task.UpdateCacheTask;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.onboarding.model.crm.CrmUpdateScamRequest;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

/**
 * @author phat.duong
 * on 9/8/2025
 **/
@Processor(name = "crm-update-status")
public class CrmUpdateScamStatusJob extends PlatformJob<PlatformData<CrmUpdateScamRequest, CrmResponse>, CrmUpdateScamRequest, CrmResponse, OnboardingConfig> {
    public CrmUpdateScamStatusJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<PlatformData<CrmUpdateScamRequest, CrmResponse>, CrmUpdateScamRequest, CrmResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadCrmConfigTask<>(),
                new CrmGetUserProfileTask<>(),
                new UpdateCacheTask()
        );
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

    @Override
    protected List<AbstractListener<PlatformData<CrmUpdateScamRequest, CrmResponse>, CrmUpdateScamRequest, CrmResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(new InsertTrackingUpdateListener());
    }
}
