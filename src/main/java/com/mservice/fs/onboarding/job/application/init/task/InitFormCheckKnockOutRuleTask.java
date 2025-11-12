package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.AbsMidKnockOutRuleAITask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.application.DeviceInfo;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;


/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class InitFormCheckKnockOutRuleTask extends AbsMidKnockOutRuleAITask<InitFormRequest, InitFormResponse> {

    @Override
    protected KnockOutRuleResponse getKnockOutRuleResponse(UserProfileInfo userProfileInfo, OnboardingData<InitFormRequest, InitFormResponse> jobData, ServiceObInfo serviceObInfo, TaskData taskData) throws Exception, BaseException {
        KnockOutRuleResponse knockOutRuleResponse = super.getKnockOutRuleResponse(userProfileInfo, jobData, serviceObInfo, taskData);
        taskData.setContent(knockOutRuleResponse);
        return knockOutRuleResponse;
    }
    protected void fillResponseDataWhenReject(InitFormResponse response, OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        applicationData.setStatus(ApplicationStatus.REJECTED_BY_KNOCKOUT_RULE);
        applicationData.setState(ApplicationStatus.REJECTED_BY_KNOCKOUT_RULE.getState());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        response.setApplicationData(applicationData);
    }

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        return jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }

    @Override
    protected boolean isAsync() {
        return false;
    }

    @Override
    protected void setDeviceInfo(ScreenLog startScreenLog, InitFormRequest request) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setNfcAvailable(Utils.isNotEmpty(request.getIsNfcAvailable()) ? request.getIsNfcAvailable() : false);
        startScreenLog.setDeviceInfo(deviceInfo);
    }

    @Override
    protected void processWithResponse(TaskData taskData, KnockOutRuleResponse packageResponse, OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        taskData.setRequest(applicationForm.getApplicationData().getApplicationId());
    }
}
