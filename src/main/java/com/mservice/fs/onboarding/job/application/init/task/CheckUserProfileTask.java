package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author hoang.thai
 * on 12/7/2023
 */
public class CheckUserProfileTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "CHECK_USER_PROFILE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public CheckUserProfileTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        String serviceId = onboardingData.getServiceId();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        if (!serviceObInfo.isMatchAction(Action.CHECK_USER_PROFILE, onboardingData.getProcessName())) {
            Log.MAIN.info("ServiceId {} does not need to check Userprofile, by pass...", serviceId);
        } else {
            ActionInfo actionInfo = serviceObInfo.getActionInfo(Action.CHECK_USER_PROFILE, onboardingData.getProcessName());
            UserProfileInfo userProfileInfo = onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
            List<String> fieldNames = actionInfo.getAllowFields();
            for (String fieldName : fieldNames) {
                Field field = UserProfileInfo.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (Utils.isEmpty(field.get(userProfileInfo))) {
                    Log.MAIN.info("Field {} is empty need to kyc", fieldName);
                    throw new BaseException(OnboardingErrorCode.CHECK_KYC_FAIL);
                }
            }
        }
        finish(onboardingData, taskData);
    }

    @Override
    protected boolean isAsync() {
        return true;
    }
}
