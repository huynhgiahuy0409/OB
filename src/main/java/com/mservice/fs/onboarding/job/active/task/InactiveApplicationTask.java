package com.mservice.fs.onboarding.job.active.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.BatchUpdatingStatusProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.lock.InactiveRequest;
import com.mservice.fs.processor.TaskData;

public class InactiveApplicationTask extends OnboardingTask<InactiveRequest, OnboardingResponse> {

    public static final TaskName NAME = () -> "ACTIVATE_LIST";

    public InactiveApplicationTask() {
        super(NAME);
    }

    @Autowire
    private BatchUpdatingStatusProcessor batchUpdatingStatusProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<InactiveRequest, OnboardingResponse> platformData) throws BaseException, Exception, ValidatorException {
        batchUpdatingStatusProcessor.execute(platformData.getRequest());
        OnboardingResponse response = new OnboardingResponse();
        response.setResultCode(CommonErrorCode.SUCCESS);
        platformData.setResponse(response);
        finish(platformData, taskData);
    }
}
