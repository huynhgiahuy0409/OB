package com.mservice.fs.onboarding.job.contract.store.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.processor.TaskData;

public class ModifiedResponse extends OnboardingTask<StoreContractRequest, StoreContractResponse> {

    public static TaskName NAME = () -> "MODIFY_RESPONSE";

    public ModifiedResponse() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        StoreContractResponse response = new StoreContractResponse();
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
