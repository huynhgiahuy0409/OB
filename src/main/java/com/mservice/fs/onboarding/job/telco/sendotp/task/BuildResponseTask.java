package com.mservice.fs.onboarding.job.telco.sendotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.config.TelcoConfig;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.processor.TaskData;

public class BuildResponseTask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "BUILD_RESPONSE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        TelcoConfig telcoConfig = serviceObInfo.getTelcoConfig();

        GenerateOtpResponse response = new GenerateOtpResponse();
        setDataResponse(response, telcoConfig, applicationData);
        jobData.setResponse(response);

        finish(jobData, taskData);
    }

    private void setDataResponse(GenerateOtpResponse response, TelcoConfig otpConfigInfo, ApplicationData applicationData) {
        OtpInfo otpInfo = applicationData.getOtpInfo();

        applicationData.setStatus(ApplicationStatus.GENERATED_OTP_TELCO);
        applicationData.setState(ApplicationState.PENDING);

        response.setMaxGenerateTimes(otpConfigInfo.getMaxGenerateOtpTimes());
        response.setMaxVerifyTimes(otpConfigInfo.getMaxVerifyOtpTimes());
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setCurrentGenerateTimes(otpInfo.getCurrentTimesGenerate() + 1);
        response.setCurrentVerifyTimes(otpInfo.getCurrentTimesVerify());
        response.setApplicationId(applicationData.getApplicationId());
        response.setResultCode(CommonErrorCode.SUCCESS);
    }
}
