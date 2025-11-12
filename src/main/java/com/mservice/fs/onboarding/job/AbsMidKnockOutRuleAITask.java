package com.mservice.fs.onboarding.job;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.ai.LenderId;
import com.mservice.fs.onboarding.model.ai.ScreenId;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleRequest;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

public abstract class AbsMidKnockOutRuleAITask<T extends OnboardingRequest, R extends OnboardingResponse> extends KnockOutRuleAiTask<T,R> {

    @Override
    protected KnockOutRuleRequest createPartnerRequest(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, T request, ServiceObInfo serviceObInfo) throws Exception, BaseException {
        KnockOutRuleRequest partnerRequest = super.createPartnerRequest(userProfileInfo, jobData, request, serviceObInfo);
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(jobData.getPartnerId());
        if (partnerConfig != null && partnerConfig.isApplyKnockOutRuleLenderId()) {
            Log.MAIN.info("APPLY KNOCKOUT RULE BY LENDER ID [{}]", partnerConfig.getId());
            ScreenLog screenLog = partnerRequest.getStartScreenLog();
            screenLog.setScreenId(ScreenId.MID_SCREEN);
            screenLog.setMessageType(AIMessageType.MID_SCREEN_LOG);
            partnerRequest.setMessageType(AIMessageType.MID_SCREEN_LOG);
            partnerRequest.setMidScreenLog(screenLog);
            partnerRequest.setLenderId(LenderId.valueOf(partnerConfig.getLenderIdAI()));
        }
        return partnerRequest;
    }
}
