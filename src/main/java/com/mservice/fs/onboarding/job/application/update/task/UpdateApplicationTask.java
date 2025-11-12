package com.mservice.fs.onboarding.job.application.update.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.jdbc.UpdateApplicationDataProcessor;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.update.UpdateApplicationRequest;
import com.mservice.fs.processor.TaskData;

public class UpdateApplicationTask extends OnboardingTask<UpdateApplicationRequest, OnboardingResponse> {

    public static final TaskName NAME = () -> "UPDATE_APPLICATION";

    public UpdateApplicationTask() {
        super(NAME);
    }

    @Autowire(name = "UpdateApplication")
    private UpdateApplicationDataProcessor updateApplicationDataProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<UpdateApplicationRequest, OnboardingResponse> onboardingData) throws BaseException, Exception {
        UpdateApplicationRequest request = onboardingData.getRequest();

        updateApplicationDataProcessor.store(request, onboardingData);

        OnboardingResponse response = new OnboardingResponse();
        response.setResultCode(OnboardingErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
