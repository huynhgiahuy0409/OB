package com.mservice.fs.onboarding.job.application.submit.listener;

import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSubmitTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.Utils;

public class RequestOtpLogListener<T extends ConfirmRequest, R extends OnboardingResponse> extends AbsAIListener<T, R> {

    private static final String NAME = "PUB_REQUEST_OTP_MSG";

    public RequestOtpLogListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<T, R> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();

        PlutusOTPRequestLog build = PlutusOTPRequestLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()))
                .setProductId(Utils.nullToEmpty(aiConfig.getProductId()))
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(ScreenId.OTP_REQUEST)
                .setMomoLoanAppId(onboardingData.getRequest().getApplicationId())
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setTrialTime(applicationData.getOtpInfo().getCurrentTimesGenerate())
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .build();
        Log.MAIN.info("Request OTP log: {} ", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) {
        return LoanInfoMessageType.OTP_REQUEST_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) {
        //condition: send adapter confirm-face-matching -> pub message.
        //CLO: not send adapter but generate otp at confirm-face-matching.
        GenerateOtpAdapterResponse adapterDefaultResponse = onboardingData.getTaskData(OnboardingSubmitTask.NAME).getContent();
        return (Utils.isNotEmpty(adapterDefaultResponse) && adapterDefaultResponse.isGenerateOtp()) || (!serviceObInfo.isMatchAction(Action.SEND_ADAPTER_SUBMIT, onboardingData.getProcessName()) && serviceObInfo.isGenerateOtpWhenSubmit()) ;
    }

    @Override
    protected String getPartnerResponse(OnboardingData<T, R> onboardingData) {
        return null;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<T, R> onboardingData) {
        ApplicationForm applicationForm = onboardingData.getTaskData(ApplicationTask.NAME).getContent();
        return applicationForm.getApplicationData();
    }
}
