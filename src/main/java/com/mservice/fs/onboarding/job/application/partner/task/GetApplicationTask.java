package com.mservice.fs.onboarding.job.application.partner.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.onboarding.service.GetApplicationProcessor;
import com.mservice.fs.onboarding.utils.OnboardingErrorCode;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 10/24/2024
 **/
public class GetApplicationTask extends OnboardingTask<ApplicationPartnerRequest, ApplicationPartnerResponse> {

    public static final TaskName NAME = () -> "GET_APPLICATION";

    public GetApplicationTask() {
        super(NAME);
    }

    @Autowire
    private GetApplicationProcessor getApplicationProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse> onboardingData) throws BaseException, Exception {
        ApplicationPartnerRequest request = onboardingData.getRequest();
        ApplicationData applicationData = getApplicationProcessor.run(request.getApplicationId(), request.getPhoneNumber());
        if (applicationData == null && Utils.isEmpty(onboardingData.getInitiatorId())) {
            throw new BaseException(OnboardingErrorCode.CONTRACT_NOT_FOUND);
        }
        onboardingData.getBase().setInitiatorId(applicationData == null ? onboardingData.getInitiatorId() : applicationData.getAgentId());
        finish(onboardingData, taskData);
    }
}