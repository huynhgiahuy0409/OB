package com.mservice.fs.onboarding.job.application.partner.task;

import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
public class ApplicationGetUserProfileTask extends AbsGetUserProfileTask<ApplicationPartnerRequest, ApplicationPartnerResponse> {

    @Override
    protected void checkUserProfileWithAction(UserProfileInfo userProfileInfo, OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse> jobData, ServiceObInfo serviceObInfo, ActionInfo actionCheckUserProfile) throws Exception, BaseException, ValidatorException {

        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkKycFinalSubmit(actionCheckUserProfile, userProfileInfo);
        if (onboardingErrorCode == null) {
            return;
        }
        Log.MAIN.info("Data user profile is invalid - agent: {} - serviceId: {} - processName: {}", jobData.getInitiatorId(), jobData.getServiceId(), jobData.getProcessName());
        ApplicationPartnerResponse response = new ApplicationPartnerResponse();
        response.setResultCode(onboardingErrorCode);
        jobData.setResponse(response);
    }
}
