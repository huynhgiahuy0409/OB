package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

public class MidKorRejectScreenLogListener<T extends OnboardingRequest, R extends OnboardingResponse> extends KorRejectScreenLogListener<T,R> {

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) throws BaseException, ValidatorException, Exception {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(onboardingData.getPartnerId());
        if (partnerConfig != null && partnerConfig.isApplyKnockOutRuleLenderId()) {
            return LoanInfoMessageType.MID_SCREEN_LOG;
        }
        return LoanInfoMessageType.START_SCREEN_LOG;
    }
}
