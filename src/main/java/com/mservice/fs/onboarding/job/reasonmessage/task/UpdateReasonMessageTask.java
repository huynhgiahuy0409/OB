package com.mservice.fs.onboarding.job.reasonmessage.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.jdbc.UpdateReasonMessageProcessor;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageRequest;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

public class UpdateReasonMessageTask extends OnboardingTask<ReasonMessageRequest, ReasonMessageResponse> {

    public static final TaskName TASK_NAME = () -> "UPDATE_REASON_MESSAGE";

    @Autowire(name = "UpdateReasonMessage")
    private UpdateReasonMessageProcessor updateReasonMessageProcessor;

    public UpdateReasonMessageTask() {
        super(TASK_NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        ReasonMessageRequest request = onboardingData.getRequest();
        ReasonMessageResponse response = new ReasonMessageResponse();

        ApplicationData applicationData = updateReasonMessageProcessor.updateReasonMessage(request.getApplicationId(),
                onboardingData.getInitiatorId(), request.getReasonMessage(), request.getApplicationStatus(),
                request.getReasonId(), onboardingData.getServiceId());

        if (Utils.isNotEmpty(applicationData)) {
            Log.MAIN.info("Update reason message success");
            taskData.setContent(applicationData);
            response.setResultCode(OnboardingErrorCode.SUCCESS);
        } else {
            Log.MAIN.info("Update reason message fail");
            response.setResultCode(OnboardingErrorCode.UPDATE_REASON_MESSAGE_FAIL);
        }
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }

}
