package com.mservice.fs.onboarding.job.sign.generateotp.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ai.LendingFlow;
import com.mservice.fs.onboarding.model.ai.LoanAppStatus;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.ai.PlutusOTPRequestLog;
import com.mservice.fs.onboarding.model.ai.ScreenId;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

public class RequestOtpLogListener extends AbsAIListener<GenerateOtpRequest, GenerateOtpResponse> {

    private static final String NAME = "PUB_REQUEST_OTP_MSG";

    public RequestOtpLogListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();

        GenerateOtpResponse response = onboardingData.getResponse();
        PlutusOTPRequestLog build = PlutusOTPRequestLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(aiConfig.getLoanProductCode())
                .setProductId(aiConfig.getProductId())
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(ScreenId.OTP_REQUEST)
                .setMomoLoanAppId(onboardingData.getRequest().getApplicationId())
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setTrialTime(response.getCurrentGenerateTimes())
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .build();
        Log.MAIN.info("Request OTP log: {}", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData) {
        return LoanInfoMessageType.OTP_REQUEST_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        return true;
    }

    @Override
    protected String getPartnerResponse(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData) {
        return CommonConstant.STRING_EMPTY;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData) {
        ApplicationData applicationData = onboardingData.getTaskData(GetApplicationTask.NAME).getContent();
        return applicationData;
    }
}
