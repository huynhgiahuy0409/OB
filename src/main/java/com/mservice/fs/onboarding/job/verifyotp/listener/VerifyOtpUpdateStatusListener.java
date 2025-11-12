package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.UpdateApplicationStatusListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.verifyotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.utils.Utils;

public class VerifyOtpUpdateStatusListener extends UpdateApplicationStatusListener<VerifyOtpRequest, VerifyOtpResponse> {

    @Override
    protected String getApplicationId(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();

        return applicationData.getApplicationId();
    }

    @Override
    protected ApplicationStatus getStatus(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();

        return applicationData.getStatus();
    }

    @Override
    protected boolean isActive(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return Utils.isNotEmpty(applicationForm);
    }

    @Override
    protected String getPartnerApplicationId(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        if (jobData.getResponse().getResultCode().equals(CommonErrorCode.SUCCESS.getCode())) {
            VerifyOtpAdapterResponse adapterResponse = jobData.getTaskData(SendAdapterTask.NAME).getContent();
            if (Utils.isNotEmpty(adapterResponse) && Utils.isNotEmpty(adapterResponse.getPartnerApplicationId())) {
                return adapterResponse.getPartnerApplicationId();
            }
        }
        return null;
    }

    @Override
    protected int getReasonId(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return applicationForm.getApplicationData().getReasonId();
    }

    @Override
    protected String getReasonMessage(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return applicationForm.getApplicationData().getReasonMessage();
    }
}
