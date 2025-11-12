package com.mservice.fs.onboarding.job;

import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.processor.AbstractListener;

/**
 * @author hoang.thai
 * on 11/3/2023
 */
public abstract class OnboardingListener<T extends OnboardingRequest, R extends OnboardingResponse> extends AbstractListener<OnboardingData<T, R>, T, R, OnboardingConfig> {

    public OnboardingListener(String name) {
        super(name);
    }

}
