package com.mservice.fs.onboarding.job;

import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;

public abstract class OnboardingSendPlatformListener<T extends OnboardingRequest, R extends OnboardingResponse> extends SendingPlatformListener<OnboardingData<T, R>, T, R, OnboardingConfig> {

    public static final String NAME = "SEND_PLATFORM_LISTENER";

    protected OnboardingSendPlatformListener() {
        super(NAME);
    }
}
