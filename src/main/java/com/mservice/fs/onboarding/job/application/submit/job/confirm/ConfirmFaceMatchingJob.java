package com.mservice.fs.onboarding.job.application.submit.job.confirm;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.job.confirm.task.ConfirmCheckLoanDeciderTask;
import com.mservice.fs.onboarding.job.application.submit.job.confirm.task.ValidateConfirmFaceMatching;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.Task;

/**
 * @author hoang.thai
 * on 1/9/2024
 */
@Processor(name = {OnboardingProcessor.CONFIRM_FACE_MATCHING})
public class ConfirmFaceMatchingJob extends ConfirmActionJob<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> {

    public ConfirmFaceMatchingJob(String name) {
        super(name);
    }

    @Override
    protected Task<OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse>, ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse, OnboardingConfig> getValidateTask() {
        return new ValidateConfirmFaceMatching();
    }

    @Override
    protected Task<OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse>, ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse, OnboardingConfig> getCheckLoanDeciderTask() {
        return new ConfirmCheckLoanDeciderTask();
    }
}
