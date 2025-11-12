package com.mservice.fs.onboarding.job.reasonmessage.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.reasonmessage.task.UpdateReasonMessageTask;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageRequest;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

public class PushEventAIListener extends AbsAIListener<ReasonMessageRequest, ReasonMessageResponse> {

    private static final String NAME = "PUB_PARTNER_RESULT_MSG";

    public PushEventAIListener() {
        super(NAME);
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData) {
        return LoanInfoMessageType.LOAN_PARTNER_RESULT_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        return onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData) {
        return "";
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData) {
        return onboardingData.getTaskData(UpdateReasonMessageTask.TASK_NAME).getContent();
    }

}
