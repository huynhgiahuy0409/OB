package com.mservice.fs.onboarding.job.verifyotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;

import java.util.Optional;

public class ValidateTask extends OnboardingTask<VerifyOtpRequest, VerifyOtpResponse> {

    public static final TaskName NAME = () -> "VALIDATE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ValidateTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        OtpConfig otpConfig = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpInfo = applicationData.getOtpInfo();

        validateData(otpInfo, otpConfig, applicationData, jobData);
        finish(jobData, taskData);
    }

    private void validateData(OtpInfo otpInfo, OtpConfig otpConfig, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException {
        int currentTimesVerify = otpInfo.getCurrentTimesVerify() + 1;
        int maxVerifyOtpTimes = otpConfig.getMaxVerifyOtpTimes();

        VerifyOtpResponse response = new VerifyOtpResponse();

        if (currentTimesVerify > maxVerifyOtpTimes) {
            Log.MAIN.info("User exceeded number of times verify otp");
            setDataResponse(response, otpInfo, otpConfig, applicationData, jobData);
            setResultOtpLimit(response, otpConfig, applicationData, jobData);
        }
    }

    private void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, OtpConfig otpConfigInfo, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());

        response.setMaxGenerateTimes(maxGenerateOtpTimes);
        response.setMaxVerifyTimes(maxVerifyOtpTimes);
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setRemainingVerifyTimes(remainingVerifyTimes);
        response.setCurrentVerifyTimes(currentVerifyTimes);
        response.setApplicationData(applicationData);
        response.setApplicationId(applicationData.getApplicationId());
    }

    protected void setResultOtpLimit(VerifyOtpResponse response, OtpConfig otpConfig, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        applicationData.setStatus(ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_EXCEED);
        applicationData.setState(ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_EXCEED.getState());

        long unlockOtpTimeInMillis = System.currentTimeMillis() + otpConfig.getResetOtpInMillis();
        OtpInfo otpInfo = applicationData.getOtpInfo();
        otpInfo.setUnlockOtpTimeInMillis(unlockOtpTimeInMillis);

        response.setResultCode(OnboardingErrorCode.OTP_VERIFY_LIMIT);
        jobData.setResponse(response);
    }


}
