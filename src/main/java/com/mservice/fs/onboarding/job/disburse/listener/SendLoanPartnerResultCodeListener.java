package com.mservice.fs.onboarding.job.disburse.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

public class SendLoanPartnerResultCodeListener extends AbsAIListener<OnboardingDisburseRequest, OnboardingDisburseResponse> {
// topic aiServiceUpdateStatus

    private static final String NAME = "SEND_LOAN_PARTNER_RESULT_CODE";

    public SendLoanPartnerResultCodeListener() {
        super(NAME);
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) throws BaseException, ValidatorException, Exception {
        return LoanInfoMessageType.LOAN_PARTNER_RESULT_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        return CommonErrorCode.SUCCESS.getCode().equals(onboardingData.getResponse().getResultCode());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) {
        return onboardingData.getRequest().getRawPartnerRequest();
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) {
        return onboardingData.getTaskData(UpdatingStatusTask.NAME).getContent();
    }

}
