package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class InitFormModifiedResponseTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "SET_RESPONSE";

    public InitFormModifiedResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> onboardingData) throws BaseException, Exception {
        ApplicationForm applicationForm = onboardingData.getTaskData(HandlePendingFormTask.NAME).getContent();
        PackageCache packageCache = onboardingData.getTaskData(InitFormPackageAi.NAME).getContent();
        OnboardingUtils.createDataForEmptyPackage(applicationForm, packageCache);
        InitFormResponse response = new InitFormResponse();
        response.setApplicationData(applicationForm.getApplicationData());
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }

}
