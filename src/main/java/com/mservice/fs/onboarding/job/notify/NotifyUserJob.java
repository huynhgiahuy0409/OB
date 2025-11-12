package com.mservice.fs.onboarding.job.notify;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Processor;
import com.mservice.fs.job.PlatformJob;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.notify.listener.PushNotifyCacheListener;
import com.mservice.fs.onboarding.job.notify.task.ValidateTask;
import com.mservice.fs.onboarding.job.notify.task.ModifyResponseTask;
import com.mservice.fs.onboarding.job.notify.task.SendPlatformTask;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserRequest;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserResponse;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;

@Processor(name = "noti-remind")
public class NotifyUserJob extends PlatformJob<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig> {


    public NotifyUserJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new ValidateTask(),
                new SendPlatformTask(),
                new ModifyResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new PushNotifyCacheListener()
        );
    }


    @Override
    public ErrorCode getSystemBugErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }

    @Override
    public ErrorCode getInvalidRequestErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
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
    protected boolean parseResultMessage() {
        return true;
    }
}
