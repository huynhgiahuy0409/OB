package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.processor.Task;

public abstract class OnboardingTask<T extends OnboardingRequest, R extends OnboardingResponse> extends Task<OnboardingData<T, R>, T, R, OnboardingConfig> {

    public OnboardingTask(TaskName taskName) {
        super(taskName);
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.TASK_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }

}
