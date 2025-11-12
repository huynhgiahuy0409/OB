package com.mservice.fs.onboarding.job.application.partner.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
public class ModifiedResponseTask extends OnboardingTask<ApplicationPartnerRequest, ApplicationPartnerResponse> {
    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    public ModifiedResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse> onboardingData) throws BaseException, Exception, ValidatorException {

        ApplicationPartnerResponse response = new ApplicationPartnerResponse();
        UserProfileInfo userProfileInfoCache = onboardingData.getTaskData(GetCacheTask.NAME).getContent();
        UserProfileInfo userProfileInfo = onboardingData.getTaskData(ApplicationGetUserProfileTask.NAME).getContent();
        if (userProfileInfoCache != null) {
            userProfileInfoCache.setIdBackImageKyc(userProfileInfo.getIdBackImageKyc());
            userProfileInfoCache.setIdFrontImageKyc(userProfileInfo.getIdFrontImageKyc());
            userProfileInfoCache.setImageFaceMatching(userProfileInfo.getImageFaceMatching());
            response.setUserProfileInfo(userProfileInfoCache);
        } else {
            Log.MAIN.info("Cache empty - use data user profile");
            response.setUserProfileInfo(userProfileInfo);
        }
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
