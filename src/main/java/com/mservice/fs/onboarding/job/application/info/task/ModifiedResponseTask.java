package com.mservice.fs.onboarding.job.application.info.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.processor.TaskData;

public class ModifiedResponseTask extends OnboardingTask<ApplicationRequest, ApplicationResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    public ModifiedResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationData(onboardingData.getTaskData(GetApplicationTask.NAME).getContent());
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
