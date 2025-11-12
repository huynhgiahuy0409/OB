package com.mservice.fs.onboarding.job.notinewpackage.listener;

import com.mservice.fs.listener.NotificationListener;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.notipackage.OnboardingNotiRequest;

public class NotifyChangedStatusListener extends NotificationListener<OnboardingData<OnboardingNotiRequest, OnboardingResponse>, OnboardingNotiRequest, OnboardingResponse, OnboardingConfig> {

    @Override
    protected boolean isActive(OnboardingData<OnboardingNotiRequest, OnboardingResponse> jobData) {
        return jobData.getResponse().getResultCode() == CommonErrorCode.SUCCESS.getCode();
    }
}
