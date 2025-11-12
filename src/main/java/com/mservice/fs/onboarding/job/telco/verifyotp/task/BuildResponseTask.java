package com.mservice.fs.onboarding.job.telco.verifyotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.job.verifyotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.config.TelcoConfig;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

public class BuildResponseTask extends OnboardingTask<VerifyOtpRequest, VerifyOtpResponse> {

    public static final TaskName NAME = () -> "BUILD_RESPONSE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException, Exception, ValidatorException {

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpInfo = applicationData.getOtpInfo();

        VerifyOtpResponse response = new VerifyOtpResponse();

        setDataResponse(response, otpInfo, serviceObInfo.getTelcoConfig(), applicationData);

        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    private void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, TelcoConfig otpConfigInfo, ApplicationData applicationData) {
        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());

        applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_TELCO_SUCCESS);
        applicationData.setState(ApplicationStatus.VERIFIED_OTP_TELCO_SUCCESS.getState());

        response.setMaxGenerateTimes(maxGenerateOtpTimes);
        response.setMaxVerifyTimes(maxVerifyOtpTimes);
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setRemainingVerifyTimes(remainingVerifyTimes);
        response.setCurrentVerifyTimes(currentVerifyTimes);
        response.setApplicationData(applicationData);
        response.setApplicationId(applicationData.getApplicationId());

        response.setResultCode(CommonErrorCode.SUCCESS);
    }
}
