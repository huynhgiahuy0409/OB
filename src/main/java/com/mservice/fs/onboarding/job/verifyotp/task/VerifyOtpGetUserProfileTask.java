package com.mservice.fs.onboarding.job.verifyotp.task;

import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import lombok.SneakyThrows;

/**
 * @author phat.duong
 * on 2/24/2025
 **/
public class VerifyOtpGetUserProfileTask extends AbsGetUserProfileTask<VerifyOtpRequest, VerifyOtpResponse> {

    @SneakyThrows
    @Override
    protected boolean isValidCondition(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, TaskData taskData) {
        return getConfig().getVerifyOtpUpdateLinkS3Partners().contains(jobData.getPartnerId());
    }
}