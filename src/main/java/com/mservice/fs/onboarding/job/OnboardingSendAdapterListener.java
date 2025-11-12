package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.listener.SendAdapterListener;
import com.mservice.fs.model.AdapterResponse;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.task.SendAdapterTask;

public abstract class OnboardingSendAdapterListener<T extends OnboardingRequest, R extends OnboardingResponse, A extends AdapterResponse> extends SendAdapterListener<OnboardingData<T, R>, T, R, A, OnboardingConfig> {

    protected OnboardingSendAdapterListener() {
        super("SEND_ADAPTER_LISTENER");
    }
}
