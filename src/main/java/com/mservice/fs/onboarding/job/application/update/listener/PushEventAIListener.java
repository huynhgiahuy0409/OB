package com.mservice.fs.onboarding.job.application.update.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.api.update.UpdateApplicationRequest;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.utils.Utils;

public class PushEventAIListener extends AbsAIListener<UpdateApplicationRequest, OnboardingResponse> {

    private static final String NAME = "PUB_PARTNER_RESULT_MSG";

    public PushEventAIListener() {
        super(NAME);
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<UpdateApplicationRequest, OnboardingResponse> onboardingData) {
        return LoanInfoMessageType.LOAN_PARTNER_RESULT_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<UpdateApplicationRequest, OnboardingResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        return onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode()) && Utils.isNotEmpty(onboardingData.getRequest().getRawPartnerRequest());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<UpdateApplicationRequest, OnboardingResponse> onboardingData) {
        return onboardingData.getRequest().getRawPartnerRequest();
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<UpdateApplicationRequest, OnboardingResponse> onboardingData) {
        UpdateApplicationRequest applicationRequest = new UpdateApplicationRequest();
        ApplicationData applicationData = new ApplicationData();
        applicationData.setApplicationId(applicationRequest.getApplicationId());

        try {
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
            PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(onboardingData.getPartnerId());
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setLenderId(partnerConfig.getLenderIdAI());
            applicationData.setChosenPackage(packageInfo);
        } catch (BaseException | Exception | ValidatorException e) {
            // skip
        }

        return applicationData;
    }

}
