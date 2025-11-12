package com.mservice.fs.onboarding.job.telco.sendotp.task;

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
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.*;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.Map;

public class ValidateTask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "VALIDATE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ValidateTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        TelcoConfig telcoConfig = serviceObInfo.getTelcoConfig();

        modifiedDataOtpInfo(telcoConfig, applicationData);
        validateData(telcoConfig, applicationData, jobData);

        finish(jobData, taskData);
    }

    private void modifiedDataOtpInfo(TelcoConfig otpConfig, ApplicationData applicationData) {
        OtpInfo otpInfo = applicationData.getOtpInfo();
        if (Utils.isEmpty(otpInfo)) {
            otpInfo = new OtpInfo();
            otpInfo.setMaxGenerateTimes(otpConfig.getMaxGenerateOtpTimes());
            otpInfo.setMaxVerifyTimes(otpConfig.getMaxVerifyOtpTimes());
            otpInfo.setValidOtpInMillis(otpConfig.getValidOtpInMillis());

            applicationData.setOtpInfo(otpInfo);
        }
    }

    private void validateData(TelcoConfig otpConfig, ApplicationData applicationData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException {
        OtpInfo otpInfo = applicationData.getOtpInfo();
        int currentTimesGenerate = otpInfo.getCurrentTimesGenerate() + 1;
        int maxGenerateOtpTimes = otpConfig.getMaxGenerateOtpTimes();

        if (currentTimesGenerate > maxGenerateOtpTimes) {
            Log.MAIN.info("User exceeded number of times generate otp");
            setDataResponse(otpConfig, applicationData, jobData);
            OnboardingUtils.putDateToTemplateMap(jobData.getTemplateModel(), otpInfo.getUnlockOtpTimeInMillis());
        }
    }

    private void setDataResponse(TelcoConfig otpConfigInfo, ApplicationData applicationData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        long unlockOtpTimeInMillis = System.currentTimeMillis() + otpConfigInfo.getResetOtpInMillis();

        OtpInfo otpInfo = applicationData.getOtpInfo();
        otpInfo.setUnlockOtpTimeInMillis(unlockOtpTimeInMillis);

        applicationData.setStatus(ApplicationStatus.REJECTED_BY_LIMITED_GENERATED_OTP_TELCO_EXCEED);
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