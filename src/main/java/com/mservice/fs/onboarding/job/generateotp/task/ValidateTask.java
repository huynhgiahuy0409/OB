package com.mservice.fs.onboarding.job.generateotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;


public class ValidateTask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "VALIDATE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ValidateTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();

        PartnerConfig partnerConfig = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                .getPartnerConfig(jobData.getPartnerId());

        OtpConfig otpConfig = partnerConfig.getOtpConfig();

        modifiedDataOtpInfo(otpConfig, applicationData);
        validateData(otpConfig, applicationData, jobData);

        finish(jobData, taskData);
    }

    private void modifiedDataOtpInfo(OtpConfig otpConfig, ApplicationData applicationData) {
        OtpInfo otpInfo = applicationData.getOtpInfo();
        if (Utils.isEmpty(otpInfo)) {
            otpInfo = new OtpInfo();
            otpInfo.setMaxGenerateTimes(otpConfig.getMaxGenerateOtpTimes());
            otpInfo.setMaxVerifyTimes(otpConfig.getMaxVerifyOtpTimes());
            otpInfo.setValidOtpInMillis(otpConfig.getValidOtpInMillis());
            otpInfo.setOtpLength(otpConfig.getOtpLength());
            applicationData.setOtpInfo(otpInfo);
        }
    }

    private void validateData(OtpConfig otpConfig, ApplicationData applicationData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException {
        OtpInfo otpInfo = applicationData.getOtpInfo();
        int currentTimesGenerate = otpInfo.getCurrentTimesGenerate() + 1;
        int maxGenerateOtpTimes = otpConfig.getMaxGenerateOtpTimes();

        if (currentTimesGenerate > maxGenerateOtpTimes) {
            Log.MAIN.info("User exceeded number of times generate otp");
            setDataResponse(otpConfig, applicationData, jobData);
        }
    }

    private void setDataResponse(OtpConfig otpConfigInfo, ApplicationData applicationData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        long unlockOtpTimeInMillis = System.currentTimeMillis() + otpConfigInfo.getResetOtpInMillis();

        OtpInfo otpInfo = applicationData.getOtpInfo();
        otpInfo.setUnlockOtpTimeInMillis(unlockOtpTimeInMillis);

        applicationData.setStatus(ApplicationStatus.REJECTED_BY_LIMITED_GENERATED_OTP_EXCEED);
        applicationData.setState(ApplicationState.LOCK);

        GenerateOtpResponse response = new GenerateOtpResponse();
        response.setMaxGenerateTimes(otpConfigInfo.getMaxGenerateOtpTimes());
        response.setMaxVerifyTimes(otpConfigInfo.getMaxVerifyOtpTimes());
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setCurrentGenerateTimes(otpInfo.getCurrentTimesGenerate() + 1);
        response.setCurrentVerifyTimes(otpInfo.getCurrentTimesVerify());
        response.setResultCode(OnboardingErrorCode.OTP_GENERATE_LIMIT);

        jobData.setResponse(response);
    }
}
