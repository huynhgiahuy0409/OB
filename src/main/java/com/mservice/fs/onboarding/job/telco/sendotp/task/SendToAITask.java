package com.mservice.fs.onboarding.job.telco.sendotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.http.SendOtpService;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.model.telco.OTPStatus;
import com.mservice.fs.onboarding.model.telco.OtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.nio.charset.StandardCharsets;

public class SendToAITask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "SEND_AI";

    @Autowire
    private SendOtpService sendOtpService;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public SendToAITask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        String telcoCourier = Utils.coalesce(applicationData.getCurrentCarrier(), applicationData.getOriginalCarrier());
        SimpleHttpResponse response = sendOtpService.sendOtp(jobData.getBase(), telcoCourier, serviceObInfo.getTelcoConfig());
        String strResp = response.getContentStr();
        Log.MAIN.info("SEND OTP TELCO RESPONSE:{}", strResp);
        OtpResponse otpResponse = Json.decodeValue(strResp.getBytes(StandardCharsets.UTF_8), OtpResponse.class);
        if (!OTPStatus.OTP_SENT_SUCCESS.name().equals(otpResponse.getOtpStatus())) {
            Log.MAIN.info("CRITICAL CALL AI FOR SEND TELCO OTP FAILED WITH RS CODE: {}", otpResponse.getOtpStatus());
            taskData.setResultCode(OnboardingErrorCode.SEND_OTP_TELCO_FAILED.getCode());
            throw new BaseException(OnboardingErrorCode.SEND_OTP_TELCO_FAILED);
        }

        applicationData.setTelcoPartner(otpResponse.getPartner());
        taskData.setResultCode(CommonErrorCode.SUCCESS.getCode());
        finish(jobData, taskData);
    }
}
