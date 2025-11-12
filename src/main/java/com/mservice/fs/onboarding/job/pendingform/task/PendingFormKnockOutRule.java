package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.AbsMidKnockOutRuleAITask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.application.DeviceInfo;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai on 10/30/2023
 */
public class PendingFormKnockOutRule extends AbsMidKnockOutRuleAITask<PendingFormRequest, PendingFormResponse> {

    @Override
    protected KnockOutRuleResponse getKnockOutRuleResponse(UserProfileInfo userProfileInfo, OnboardingData<PendingFormRequest, PendingFormResponse> jobData, ServiceObInfo serviceObInfo, TaskData taskData) throws Exception, BaseException {
        KnockOutRuleResponse knockOutRuleResponse = super.getKnockOutRuleResponse(userProfileInfo, jobData, serviceObInfo, taskData);
        taskData.setContent(knockOutRuleResponse);
        return knockOutRuleResponse;
    }

    @Override
    protected void fillResponseDataWhenReject(PendingFormResponse response, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        applicationData.setStatus(ApplicationStatus.REJECTED_BY_KNOCKOUT_RULE);
        applicationData.setState(ApplicationStatus.REJECTED_BY_KNOCKOUT_RULE.getState());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        response.setApplicationData(applicationData);
    }

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        return jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
    }

    @Override
    protected void setDeviceInfo(ScreenLog startScreenLog, PendingFormRequest request) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setNfcAvailable(Utils.isNotEmpty(request.getIsNfcAvailable()) ? request.getIsNfcAvailable() : false);
        startScreenLog.setDeviceInfo(deviceInfo);
    }

    @Override
    protected void processWithResponse(TaskData taskData, KnockOutRuleResponse packageResponse, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
        taskData.setRequest(applicationForm.getApplicationData().getApplicationId());
    }
}
