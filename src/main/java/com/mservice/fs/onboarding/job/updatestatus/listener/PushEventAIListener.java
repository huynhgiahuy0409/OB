package com.mservice.fs.onboarding.job.updatestatus.listener;

import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ai.LoanInfoLogRecord;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

public class PushEventAIListener extends AbsAIListener<UpdatingStatusRequest, UpdatingStatusResponse> {

    private static final String NAME = "PUB_PARTNER_RESULT_MSG";

    public PushEventAIListener() {
        super(NAME);
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
        return getApplicationData(onboardingData).getStatus().getMessageType();
    }

    @Override
    protected boolean isValid(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData, ServiceObInfo serviceObInfo) {
        return onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
        return onboardingData.getRequest().getRawPartnerRequest();
    }

    @Override
    protected void doMoreAction(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData, LoanInfoLogRecord.Builder loanInfoLogRecord) {
        loanInfoLogRecord.setCreatedAt(onboardingData.getRequest().getCreatedTime());
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
        return onboardingData.getTaskData(UpdatingStatusTask.NAME).getContent();
    }
}
