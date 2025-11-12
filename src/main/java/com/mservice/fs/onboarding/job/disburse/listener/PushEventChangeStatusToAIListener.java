package com.mservice.fs.onboarding.job.disburse.listener;

import com.google.protobuf.ByteString;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.google.AIService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.utils.Utils;

public abstract class PushEventChangeStatusToAIListener extends OnboardingListener<OnboardingDisburseRequest, OnboardingDisburseResponse> {

    private static final String NAME = "PUSH_EVENT_DISBURSE_TO_AI";

    private static final int SCREEN_RESULT = 9;

    public PushEventChangeStatusToAIListener(String name) {
        super(name);
    }

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    public void execute(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) throws Throwable {

        if (!CommonErrorCode.SUCCESS.getCode().equals(onboardingData.getResponse().getResultCode())) {
            Log.MAIN.info("Response error - skip pubsub");
            return;
        }

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        serviceObInfo.getAiConfig().getLoanProductCode();
        if (serviceObInfo.isMatchAction(Action.EVENT_TO_AI, onboardingData.getProcessName())) {
            ApplicationData applicationData = onboardingData.getTaskData(UpdatingStatusTask.NAME).getContent();
            long agentId = Long.parseLong(applicationData.getAgentId());
            AiConfig aiConfig = getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
            LoanInfoLogRecord loanInfoLogRecord = LoanInfoLogRecord.newBuilder()
                        .setRequestId(onboardingData.getTraceId())
                        .setAgentId(agentId)
                        .setLoanProductCode(aiConfig.getLoanProductCode())
                        .setMomoLoanAppId(applicationData.getApplicationId())
                        .setProductGroup(ProductGroup.CASH_LOAN)
                        .setProductId(aiConfig.getProductId())
                        .setMerchantId(Utils.nullToEmpty(aiConfig.getMerchantId()))
                        .setTimestamp(System.currentTimeMillis())
                        .setMessageType(getMessageType())
                        .setScreenOrder(SCREEN_RESULT)
                        .setLenderId(LenderId.valueOf(applicationData.getChosenPackage().getLenderId()))
                        .setMessageData(createMessageData(applicationData, onboardingData, agentId))
                        .build();
            Log.MAIN.info("Message end to AI [{}]", OnboardingUtils.encode(loanInfoLogRecord));
            getAiService().sendEvent(loanInfoLogRecord.toByteString());
        }
    }

    private ByteString createMessageData(ApplicationData applicationData, OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData, long agentId) throws BaseException, ValidatorException, Exception {
        OnboardingDisburseRequest request = onboardingData.getRequest();
        PackageInfo chosenPackage = applicationData.getChosenPackage();
        AiConfig aiConfig = getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        DisbursementLog disbursementLog = DisbursementLog.newBuilder()
                .setLoanProductCode(aiConfig.getLoanProductCode())
                .setProductId(aiConfig.getProductId())
                .setSelectedLoanPackage(chosenPackage.getPackageCode())
                .setAgentId(agentId)
                .setMomoLoanAppId(applicationData.getApplicationId())
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_DISBURSED)
                .setDisbursedTime(System.currentTimeMillis())
                .setLoanTenor(chosenPackage.getTenor())
                .setLoanAmount(chosenPackage.getDisbursedAmount())
                .setInterestAmount(chosenPackage.getInterestAmount())
                .setTotalLoanAmount(chosenPackage.getDisbursedAmount() + chosenPackage.getInterestAmount())
                .setPartnerCode(Utils.nullToEmpty(request.getPartnerApplicationId()))
                .setCoreTransId(String.valueOf(request.getCoreId()))
                .setPaymentStatus(PaymentStatus.LOAN_SUCCESS)
                .setContractId(applicationData.getApplicationId())
                .setBusinessPaymentType(PaymentType.PAYMENT_DISBURSEMENT)
                .build();
        Log.MAIN.info("MessageData [{}]", OnboardingUtils.encode(disbursementLog));
        return disbursementLog.toByteString();
    }

    private ServiceObInfo getServiceAIInfo(String serviceId) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(serviceId);
    }

    protected abstract LoanInfoMessageType getMessageType();

    protected abstract AIService getAiService();
}
