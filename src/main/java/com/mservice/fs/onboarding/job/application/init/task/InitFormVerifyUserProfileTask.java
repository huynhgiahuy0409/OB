package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author muoi.nong
 */
public class InitFormVerifyUserProfileTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "VERIFY_USER_PROFILE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public InitFormVerifyUserProfileTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        UserProfileInfo profileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        validateUserProfileInfo(jobData, profileInfo);
        finish(jobData, taskData);
    }

    private void validateUserProfileInfo(OnboardingData<InitFormRequest, InitFormResponse> jobData, UserProfileInfo userProfileInfo) throws BaseException, Exception {
        Log.MAIN.info("Check userProfileInfo");
        try {
            String serviceId = jobData.getServiceId();
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
            Action actionCheckUserProfile = Action.CHECK_USER_PROFILE;
            if (!serviceObInfo.isMatchAction(actionCheckUserProfile, jobData.getProcessName())) {
                Log.MAIN.info("ServiceId {} does not need to check Userprofile, by pass...", serviceId);
            } else {
                ActionInfo actionInfo = serviceObInfo.getActionInfo(actionCheckUserProfile, jobData.getProcessName());
                checkUserProfileWithAction(userProfileInfo, jobData, serviceObInfo, actionInfo);
            }
        } catch (ValidatorException e) {
            Log.MAIN.error("Get ServiceObInfo fail with serviceId {}", jobData.getServiceId());
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
    }

    protected void checkUserProfileWithAction(UserProfileInfo userProfileInfo, OnboardingData<InitFormRequest, InitFormResponse> jobData, ServiceObInfo serviceObInfo, ActionInfo actionCheckUserProfile) throws Exception, ValidatorException {
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkKycInitForm(actionCheckUserProfile, userProfileInfo);
        if (onboardingErrorCode == null) {
            return;
        }
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        InitFormResponse response = new InitFormResponse();
        jobData.putProcessNameToTemPlateModel(OnboardingProcessor.INIT_CONFIRM);
        jobData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, applicationForm.getApplicationData().getApplicationId());
        response.setResultCode(onboardingErrorCode);
        jobData.setResponse(response);
    }
}
