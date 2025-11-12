package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.task.SendingPlatformTask;

public abstract class OnboardingSendPlatformTask<T extends OnboardingRequest, R extends OnboardingResponse> extends SendingPlatformTask<OnboardingData<T, R>, T, R, OnboardingConfig> {
    public static final TaskName NAME = () -> "SEND_PLATFORM_TASK";

    protected OnboardingSendPlatformTask() {
        super(NAME);
    }

    public OnboardingSendPlatformTask(TaskName taskName) {
        super(taskName);
    }
}
