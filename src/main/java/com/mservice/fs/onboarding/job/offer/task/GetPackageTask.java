package com.mservice.fs.onboarding.job.offer.task;

import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.PackageAiTask;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.offer.OfferData;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author hoang.thai on 8/28/2023
 */
public class GetPackageTask extends PackageAiTask<OfferRequest, OfferResponse> {

    @Override
    protected String getSegmentUser(OnboardingData<OfferRequest, OfferResponse> jobData) {
        OfferData offerData = jobData.getTaskData(CacheTask.NAME).getContent();
        KnockOutRuleResponse cacheKnockOutRuleResponse = offerData.getKnockOutRuleResponse();
        if (cacheKnockOutRuleResponse != null) {
            return cacheKnockOutRuleResponse.getLoanDeciderRecord().getProfile().getSegmentUser();
        } else {
            KnockOutRuleResponse packageResponse = jobData.getTaskData(GetOfferKnockOutRuleTask.NAME).getContent();
            return packageResponse.getLoanDeciderRecord().getProfile().getSegmentUser();
        }
    }

    @Override
    protected void processWithBadRequest(GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<OfferRequest, OfferResponse> jobData, ServiceObInfo serviceObInfo) throws BaseException {
        //Do nothing
        //CLO does not call get offers in api check-status
        //CCM call get Package in check Status with UserType New and Reject and when user not found return package not throw Exception
    }
}
