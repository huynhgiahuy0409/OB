package com.mservice.fs.onboarding.job.notinewpackage.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.notipackage.OnboardingNotiRequest;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

public class BuildResponseTask extends OnboardingTask<OnboardingNotiRequest, OnboardingResponse> {

    public static final TaskName NAME = () -> "BUILD_RESPONSE";

    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingNotiRequest, OnboardingResponse> jobData) throws BaseException, Exception, ValidatorException {
        OnboardingResponse response = new OnboardingResponse();
        UserProfileInfo userProfileInfo = jobData.getTaskData(NotiGetUserProfile.NAME).getContent();
        Base base = jobData.getBase();
        base.setInitiator(userProfileInfo.getPhone());
        response.setResultCode(CommonErrorCode.SUCCESS);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.TASK_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }
}
