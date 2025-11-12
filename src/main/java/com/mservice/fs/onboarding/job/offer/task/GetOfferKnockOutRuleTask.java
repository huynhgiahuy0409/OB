package com.mservice.fs.onboarding.job.offer.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.KnockOutRuleAiTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.offer.OfferData;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author hoang.thai on 10/30/2023
 */
public class GetOfferKnockOutRuleTask extends KnockOutRuleAiTask<OfferRequest, OfferResponse> {

    @Override
    protected KnockOutRuleResponse getKnockOutRuleResponse(UserProfileInfo userProfileInfo, OnboardingData<OfferRequest, OfferResponse> jobData, ServiceObInfo serviceObInfo, TaskData taskData) throws Exception, BaseException {
        OfferData offerData = jobData.getTaskData(CacheTask.NAME).getContent();
        KnockOutRuleResponse cacheKnockOutRuleResponse = offerData.getKnockOutRuleResponse();
        if (cacheKnockOutRuleResponse != null) {
            Log.MAIN.info("Get Knock out rule response from cache: {}", JsonUtil.toString(cacheKnockOutRuleResponse));
            return cacheKnockOutRuleResponse;
        }
        KnockOutRuleResponse knockOutRuleResponse = super.getKnockOutRuleResponse(userProfileInfo, jobData, serviceObInfo, taskData);
        taskData.setContent(knockOutRuleResponse);
        return knockOutRuleResponse;
    }

    @Override
    protected boolean isCheckLoanAction(OnboardingData<OfferRequest, OfferResponse> jobData,ServiceObInfo serviceObInfo) {
        return false;
    }
}
