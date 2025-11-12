package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public class PendingFormGetUserProfileTask extends AbsGetUserProfileTask<PendingFormRequest, PendingFormResponse> {

    @Override
    protected void checkUserProfileWithAction(UserProfileInfo userProfileInfo, OnboardingData<PendingFormRequest, PendingFormResponse> jobData, ServiceObInfo serviceObInfo, ActionInfo actionCheckUserProfile) throws ReflectiveOperationException, BaseException {
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkKycInitForm(actionCheckUserProfile, userProfileInfo);
        if (onboardingErrorCode == null) {
            return;
        }
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
        PendingFormResponse response = new PendingFormResponse();
        jobData.putProcessNameToTemPlateModel(OnboardingProcessor.INIT_CONFIRM);
        jobData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, applicationForm.getApplicationData().getApplicationId());
        response.setResultCode(onboardingErrorCode);
        jobData.setResponse(response);
    }
}
