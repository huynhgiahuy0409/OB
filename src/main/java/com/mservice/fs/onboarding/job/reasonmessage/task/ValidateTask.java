package com.mservice.fs.onboarding.job.reasonmessage.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageRequest;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageResponse;
import com.mservice.fs.processor.TaskData;

import java.util.Set;

public class ValidateTask extends OnboardingTask<ReasonMessageRequest, ReasonMessageResponse> {

    private static final TaskName TASK_NAME = () -> "VALIDATE_REQUEST";
    private final Set<ApplicationStatus> updateReasonMessageStatus = Set.of(ApplicationStatus.VERIFIED_OTP_SUCCESS, ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.APPROVED_BY_LENDER, ApplicationStatus.REVIEW_BY_LENDER, ApplicationStatus.ACTIVATED_BY_LENDER, ApplicationStatus.LIQUIDATION);

    public ValidateTask() {
        super(TASK_NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ReasonMessageRequest, ReasonMessageResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        ReasonMessageRequest request = onboardingData.getRequest();
        Log.MAIN.info("Check request update status.");
        if (!updateReasonMessageStatus.contains(request.getApplicationStatus())) {
            Log.MAIN.info("Not apply for status: {} in request", request.getApplicationStatus());
            ReasonMessageResponse response = onboardingData.getResponse();
            response.setResultCode(OnboardingErrorCode.INVALID_REQUEST_UPDATE_REASON_MESSAGE);
            onboardingData.setResponse(response);
        }
        finish(onboardingData, taskData);
    }
}
