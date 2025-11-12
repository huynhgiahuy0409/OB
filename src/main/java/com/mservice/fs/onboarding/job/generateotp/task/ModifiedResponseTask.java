package com.mservice.fs.onboarding.job.generateotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.processor.TaskData;

import java.util.Optional;

public class ModifiedResponseTask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ModifiedResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        OtpConfig otpConfig = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

        ApplicationForm applicationCache = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationCache.getApplicationData();

        GenerateOtpResponse response = new GenerateOtpResponse();
        setDataResponse(response, otpConfig, applicationData);
        jobData.setResponse(response);

        finish(jobData, taskData);
    }

    private void setDataResponse(GenerateOtpResponse response, OtpConfig otpConfigInfo, ApplicationData applicationData) {
        OtpInfo otpInfo = applicationData.getOtpInfo();

        applicationData.setStatus(ApplicationStatus.GENERATED_OTP);
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
