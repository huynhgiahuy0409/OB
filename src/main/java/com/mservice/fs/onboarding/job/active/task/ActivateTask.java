package com.mservice.fs.onboarding.job.active.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.ActivateProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.lock.ActiveRequest;
import com.mservice.fs.processor.TaskData;

public class ActivateTask extends OnboardingTask<ActiveRequest, OnboardingResponse> {

    public static final TaskName NAME = () -> "UPDATE_ACTIVATE";

    @Autowire
    private ActivateProcessor activateProcessor;

    public ActivateTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ActiveRequest, OnboardingResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        ActiveRequest request = onboardingData.getRequest();
        activateProcessor.execute(request);
        OnboardingResponse response = new OnboardingResponse();
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
