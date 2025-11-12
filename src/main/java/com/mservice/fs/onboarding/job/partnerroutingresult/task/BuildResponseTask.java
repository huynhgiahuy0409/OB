package com.mservice.fs.onboarding.job.partnerroutingresult.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.partnerrouting.PlatformPartnerRoutingResponse;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingRequest;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingResponse;
import com.mservice.fs.processor.TaskData;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
public class BuildResponseTask extends OnboardingTask<PartnerRoutingRequest, PartnerRoutingResponse> {
    public static final TaskName NAME = () -> "BUILD-RESPONSE";

    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> onboardingData) throws BaseException, Exception, ValidatorException {

        PlatformPartnerRoutingResponse platformResponse = onboardingData.getTaskData(SendPlatformTask.NAME).getContent();
        PartnerRoutingResponse response = new PartnerRoutingResponse();
        response.setResultCode(platformResponse.getResultCode());
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }
}
