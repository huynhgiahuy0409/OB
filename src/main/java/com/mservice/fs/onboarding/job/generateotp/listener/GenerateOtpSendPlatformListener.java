package com.mservice.fs.onboarding.job.generateotp.listener;

import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.SendApplicationDataPlatformListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 10/8/2025
 **/
public class GenerateOtpSendPlatformListener extends SendApplicationDataPlatformListener<GenerateOtpRequest, GenerateOtpResponse> {
    @Override
    protected ApplicationData getApplicationData(OnboardingData< GenerateOtpRequest, GenerateOtpResponse > jobData) throws Exception {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm) ? applicationForm.getApplicationData() : null;
    }
}
