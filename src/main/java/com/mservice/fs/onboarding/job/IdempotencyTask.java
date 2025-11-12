package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.redis.processor.GetResponseCacheTask;

/**
 * @author phat.duong
 * on 1/15/2025
 **/
public class IdempotencyTask<T extends OnboardingRequest, R extends OnboardingResponse> extends GetResponseCacheTask<OnboardingData<T, R>, T, R, OnboardingConfig> {

    public static final TaskName NAME = () -> "GET-CACHE";

    public IdempotencyTask() {
        super(NAME);
    }

    @Override
    protected String getKey(OnboardingData<T, R> onboardingData) {
        return onboardingData.getProcessName() + ":" + onboardingData.getServiceId() + "_" + onboardingData.getInitiatorId();
    }

    @Override
    protected String createStorage(OnboardingData<T, R> onboardingData) {
        return Constant.ONBOARDING;
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
