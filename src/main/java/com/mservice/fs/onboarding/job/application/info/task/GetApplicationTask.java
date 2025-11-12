package com.mservice.fs.onboarding.job.application.info.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.service.GetApplicationProcessor;
import com.mservice.fs.onboarding.utils.OnboardingErrorCode;
import com.mservice.fs.processor.TaskData;

public class GetApplicationTask extends OnboardingTask<ApplicationRequest, ApplicationResponse> {

    public static final TaskName NAME = () -> "GET_APPLICATION";

    public GetApplicationTask() {
        super(NAME);
    }

    @Autowire
    private GetApplicationProcessor getApplicationProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData) throws BaseException, Exception {
        ApplicationRequest request = onboardingData.getRequest();
        ApplicationData applicationData = getApplicationProcessor.run(request.getApplicationId(), request.getPhoneNumber());
        if (applicationData == null) {
            throw new BaseException(OnboardingErrorCode.CONTRACT_NOT_FOUND);
        }
        ApplicationResponse applicationResponse = new ApplicationResponse();
        applicationResponse.setApplicationData(applicationData);
        taskData.setContent(applicationData);
        finish(onboardingData, taskData);
    }
}
