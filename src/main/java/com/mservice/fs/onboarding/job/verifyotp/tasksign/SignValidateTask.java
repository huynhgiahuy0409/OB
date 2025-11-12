package com.mservice.fs.onboarding.job.verifyotp.tasksign;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.verifyotp.task.ValidateTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;

public class SignValidateTask extends ValidateTask {

    public static final TaskName NAME = () -> "VALIDATE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected void setResultOtpLimit(VerifyOtpResponse response, OtpConfig otpConfig, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        applicationData.setStatus(ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_SIGN_EXCEED);
        applicationData.setState(ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_SIGN_EXCEED.getState());

        long unlockOtpTimeInMillis = System.currentTimeMillis() + otpConfig.getResetOtpInMillis();
        OtpInfo otpInfo = applicationData.getOtpInfo();
        otpInfo.setUnlockOtpTimeInMillis(unlockOtpTimeInMillis);

        response.setResultCode(OnboardingErrorCode.OTP_VERIFY_LIMIT);
        jobData.setResponse(response);
    }


}
