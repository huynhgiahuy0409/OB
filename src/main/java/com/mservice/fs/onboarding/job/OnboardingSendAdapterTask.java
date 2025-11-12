package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.AdapterResponse;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.task.SendAdapterTask;

/**
 * @author hoang.thai
 * on 11/17/2023
 */
public abstract class OnboardingSendAdapterTask<T extends OnboardingRequest, R extends OnboardingResponse, A extends AdapterResponse> extends SendAdapterTask<OnboardingData<T, R>, T, R, A, OnboardingConfig> {

    public static final TaskName NAME = () -> "SEND_ADAPTER";

    protected OnboardingSendAdapterTask() {
        super(NAME);
    }

    protected abstract String getPartnerId(OnboardingData<T, R> onboardingData);

    @Override
    protected String getKeyRouting(OnboardingData<T, R> onboardingData) {
        return getPartnerId(onboardingData);
    }
}
