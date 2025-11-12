package com.mservice.fs.onboarding.job.sign.generateotp.task;

import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import lombok.SneakyThrows;

/**
 * @author phat.duong
 * on 2/24/2025
 **/
public class OtpGetUserProfileTask extends AbsGetUserProfileTask<GenerateOtpRequest, GenerateOtpResponse> {

    @SneakyThrows
    @Override
    protected boolean isValidCondition(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, TaskData taskData) {
        return getConfig().getGenerateOtpUpdateLinkS3Partners().contains(jobData.getPartnerId());
    }
}