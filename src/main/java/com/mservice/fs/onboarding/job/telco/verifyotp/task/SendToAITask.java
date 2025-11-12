package com.mservice.fs.onboarding.job.telco.verifyotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.http.VerifyOtpService;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.telco.OTPStatus;
import com.mservice.fs.onboarding.model.telco.OtpResponse;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.nio.charset.StandardCharsets;

public class SendToAITask extends OnboardingTask<VerifyOtpRequest, VerifyOtpResponse> {

    public static final TaskName NAME = () -> "SEND_AI";

    @Autowire
    private VerifyOtpService verifyOtpService;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public SendToAITask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        SimpleHttpResponse response = verifyOtpService.verify(jobData.getBase(), jobData.getRequest(), applicationData.getTelcoPartner(), serviceObInfo.getTelcoConfig());
        String strResponse = response.getContentStr();
        Log.MAIN.info("VERIFY OTP TELCO RESPONSE:{}", strResponse);
        OtpResponse otpResponse = Json.decodeValue(strResponse.getBytes(StandardCharsets.UTF_8), OtpResponse.class);

        if (OTPStatus.INVALID_OTP.name().equals(otpResponse.getOtpStatus())) {
            Log.MAIN.info("INVALID OTP INFO:{}", otpResponse.getOtpStatus());
            applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_TELCO_FAILED);
            applicationData.setState(ApplicationStatus.VERIFIED_OTP_TELCO_FAILED.getState());
            taskData.setResultCode(OnboardingErrorCode.OTP_VERIFY_ERROR.getCode());
            throw new BaseException(OnboardingErrorCode.OTP_VERIFY_ERROR);
        }
        if (!OTPStatus.VERIFIED.name().equals(otpResponse.getOtpStatus())) {
            Log.MAIN.info("CRITICAL CALL AI FOR SEND TELCO OTP FAILED WITH RS CODE:{}", otpResponse.getOtpStatus());
            taskData.setResultCode(OnboardingErrorCode.SEND_OTP_TELCO_FAILED.getCode());
            throw new BaseException(OnboardingErrorCode.SEND_OTP_TELCO_FAILED);
        }
        taskData.setResultCode(CommonErrorCode.SUCCESS.getCode());
        finish(jobData, taskData);
    }
}
