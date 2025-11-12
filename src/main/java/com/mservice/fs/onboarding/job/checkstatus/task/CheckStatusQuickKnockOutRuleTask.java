package com.mservice.fs.onboarding.job.checkstatus.task;


import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockoutRuleTracking;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;


public class CheckStatusQuickKnockOutRuleTask extends CheckStatusKnockOutRuleTask {

    @Override
    protected boolean isCheckLoanAction(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        return true;
    }

    @Override
    protected void checkActionWhenApprove(TaskData taskData, ServiceObInfo serviceObInfo, KnockOutRuleResponse knockOutRuleResponse, UserProfileInfo userProfileInfo, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, OnboardingStatusResponse response, KnockoutRuleTracking knockoutRuleTracking) throws BaseException, Exception {
        super.checkActionWhenApprove(taskData, serviceObInfo, knockOutRuleResponse, userProfileInfo, jobData, response, knockoutRuleTracking);
    }

    @Override
    protected void addResponse(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, OnboardingStatusResponse response, AiLoanActionConfig aiLoanActionConfig) {
        taskData.setContent(aiLoanActionConfig);
    }
}
