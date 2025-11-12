package com.mservice.fs.onboarding.job.notify.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserRequest;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;

public class ModifyResponseTask extends Task<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    public ModifyResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<NotifyUserRequest, NotifyUserResponse> data) throws BaseException, Exception, ValidatorException {
        Integer serviceResultCode = Integer.parseInt(data.getTaskData(SendPlatformTask.NAME).getResultCode());
        NotifyUserResponse response = new NotifyUserResponse();
        response.setResultCode(OnboardingUtils.getErrorCode(serviceResultCode));
        data.setResponse(response);
        finish(data, taskData);
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
