package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.Utils;

public class ResponseOtpLogListener extends AbsAIListener<VerifyOtpRequest, VerifyOtpResponse> {

    private static final String NAME = "PUB_RESPONSE_OTP_MSG";

    public ResponseOtpLogListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();

        VerifyOtpResponse response = onboardingData.getResponse();
        PlutusOTPResponseLog build = PlutusOTPResponseLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(aiConfig.getLoanProductCode())
                .setProductId(aiConfig.getProductId())
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(ScreenId.OTP_RESPONSE)
                .setMomoLoanAppId(onboardingData.getRequest().getApplicationId())
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setTrialTime(response.getCurrentVerifyTimes())
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .setStatus(getStatus(onboardingData))
                .build();
        Log.MAIN.info("Response OTP log: {}", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    private OTP_Status getStatus(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        if (onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode())) {
            return OTP_Status.SUCCESS_OTP_STATUS;
        }
        return OTP_Status.FAIL_OTP_STATUS;
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        return LoanInfoMessageType.OTP_RESPONSE_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData, ServiceObInfo serviceObInfo) {
        return true;
    }

    @Override
    protected String getPartnerResponse(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        return null;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        ApplicationForm applicationForm = onboardingData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        return applicationForm.getApplicationData();
    }
}
