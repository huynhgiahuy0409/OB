package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Generics;

/**
 * @author hoang.thai
 * on 12/16/2023
 */
public class SubmitGetUserProfileTask<T extends ConfirmRequest, R extends SubmitResponse> extends AbsGetUserProfileTask<T, R> {

    protected final Class<?> responseClass;

    public SubmitGetUserProfileTask() {
        super();
        this.responseClass = Generics.getTypeParameter(this.getClass(), OnboardingResponse.class);
    }

    @Override
    protected void checkUserProfileWithAction(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, ActionInfo actionCheckUserProfile) throws ReflectiveOperationException, BaseException {
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkKycFinalSubmit(actionCheckUserProfile, userProfileInfo);
        if (onboardingErrorCode == null) {
            return;
        }
        R response = (R) Generics.createObject(responseClass);
        response.setResultCode(onboardingErrorCode);
        jobData.putProcessNameToTemPlateModel(OnboardingProcessor.FINAL_SUBMIT);
        jobData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, jobData.getRequest().getApplicationId());
        jobData.setResponse(response);
    }


}
