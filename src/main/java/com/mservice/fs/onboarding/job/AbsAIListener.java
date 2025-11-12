package com.mservice.fs.onboarding.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.google.AIService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

public abstract class AbsAIListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    private static final int SCREEN_CHANGE_STATUS = 9;

    @Autowire
    private AIService aiServiceUpdateStatus;

    @Autowire(name = "ServiceConfigInfo")
    protected DataService<ServiceObConfig> onboardingDataInfo;

    public AbsAIListener(String name) {
        super(name);
    }

    @Override
    public void execute(OnboardingData<T, R> onboardingData) throws Throwable {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.EVENT_TO_AI, onboardingData.getProcessName()) && isValid(onboardingData, serviceObInfo)) {

            ApplicationData applicationData;
            try {
                applicationData = getApplicationData(onboardingData);
            } catch (Exception e) {
                Log.MAIN.info("Can't get applicationData - skip pubsub to AI");
                return;
            }


            long agentId = Long.parseLong(onboardingData.getInitiatorId());
            AiConfig aiConfig = getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
            LoanInfoMessageType messageType = getLoanMessageType(onboardingData);
            LoanInfoLogRecord.Builder loanInfoLogRecordBuilder = LoanInfoLogRecord.newBuilder()
                    .setRequestId(Utils.nullToEmpty(getPubMessageRequestId(onboardingData)))
                    .setAgentId(agentId)
                    .setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()))
                    .setMomoLoanAppId(Utils.nullToEmpty(applicationData.getApplicationId()))
                    .setProductGroup(ProductGroup.valueOf(aiConfig.getProductGroup()))
                    .setProductId(Utils.nullToEmpty(aiConfig.getProductId()))
                    .setMerchantId(Utils.nullToEmpty(aiConfig.getMerchantId()))
                    .setTimestamp(System.currentTimeMillis())
                    .setMessageType(messageType)
                    .setScreenOrder(SCREEN_CHANGE_STATUS)
                    .setMessageData(createMessageData(onboardingData, applicationData, agentId))
                    .setLenderId(Utils.isEmpty(applicationData.getChosenPackage()) || Utils.isEmpty(applicationData.getChosenPackage().getLenderId()) ? LenderId.UNKNOWN_LENDER : LenderId.valueOf(applicationData.getChosenPackage().getLenderId()));

            doMoreAction(onboardingData, loanInfoLogRecordBuilder);

            LoanInfoLogRecord loanInfoLogRecord = loanInfoLogRecordBuilder.build();


            Log.MAIN.info("Message end to AI for type [{}] || [{}]", messageType, OnboardingUtils.encode(loanInfoLogRecord));
            aiServiceUpdateStatus.sendEvent(loanInfoLogRecord.toByteString());
        }
    }

    protected abstract LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) throws BaseException, ValidatorException, Exception;

    protected void doMoreAction(OnboardingData<T, R> onboardingData, LoanInfoLogRecord.Builder loanInfoLogRecord) {
    }

    protected abstract boolean isValid(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException;

    protected ByteString createMessageData(OnboardingData<T, R> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        PackageInfo chosenPackage = applicationData.getChosenPackage();
        LoanAppStatus loanAppStatus = Utils.isEmpty(applicationData.getStatus()) ? LoanAppStatus.LOAN_STATUS_IN_PROGRESS : applicationData.getStatus().getLoanAppStatus();
        PlutusLoanPartnerResultLog partnerResultLog = PlutusLoanPartnerResultLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()))
                .setProductId(Utils.nullToEmpty(aiConfig.getProductId()))
                .setAgentId(agentId)
                .setMomoLoanAppId(Utils.nullToEmpty(applicationData.getApplicationId()))
                .setSelectedLoanPackage(chosenPackage == null ? CommonConstant.STRING_EMPTY : Utils.nullToEmpty(chosenPackage.getPackageCode()))
                .setLoanAppStatus(loanAppStatus)
                .setResponseMessage(loanAppStatus.name())
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .setPartnerResponse(Utils.nullToEmpty(getPartnerResponse(onboardingData)))
                .build();
        Log.MAIN.info("MessageData to AI for type [{}] || [{}]", getLoanMessageType(onboardingData), OnboardingUtils.encode(partnerResultLog));
        return partnerResultLog.toByteString();
    }

    protected abstract String getPartnerResponse(OnboardingData<T, R> onboardingData);

    protected abstract ApplicationData getApplicationData(OnboardingData<T, R> onboardingData);

    protected ServiceObInfo getServiceAIInfo(String serviceId) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(serviceId);
    }

    protected String getPubMessageRequestId(OnboardingData<T, R> onboardingData) throws JsonProcessingException {
        return onboardingData.getTraceId();
    }
}
