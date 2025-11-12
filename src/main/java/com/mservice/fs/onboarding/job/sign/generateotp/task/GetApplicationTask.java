package com.mservice.fs.onboarding.job.sign.generateotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.service.GetApplicationProcessor;
import com.mservice.fs.onboarding.utils.OnboardingErrorCode;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

public class GetApplicationTask extends OnboardingTask<GenerateOtpRequest, GenerateOtpResponse> {

    public static final TaskName NAME = () -> "GET_APPLICATION";

    public GetApplicationTask() {
        super(NAME);
    }

    @Autowire
    private GetApplicationProcessor getApplicationProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData) throws BaseException, Exception {
        ApplicationForm applicationForm = onboardingData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        if (Utils.isNotEmpty(applicationForm) && Utils.isNotEmpty(applicationForm.getApplicationData())) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            taskData.setContent(applicationData);
            finish(onboardingData, taskData);
            return;
        }
        GenerateOtpRequest request = onboardingData.getRequest();
        ApplicationData applicationData = getApplicationProcessor.run(request.getApplicationId(), onboardingData.getInitiator());
        if (applicationData == null) {
            throw new BaseException(OnboardingErrorCode.CONTRACT_NOT_FOUND);
        }
        ApplicationResponse applicationResponse = new ApplicationResponse();
        applicationResponse.setApplicationData(applicationData);
        taskData.setContent(applicationData);
        finish(onboardingData, taskData);
    }
}
