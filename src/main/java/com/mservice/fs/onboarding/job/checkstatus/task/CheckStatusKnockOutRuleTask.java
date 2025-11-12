package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.KnockOutRuleAiTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.application.DeviceInfo;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.SegmentData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai on 10/30/2023
 */
public class CheckStatusKnockOutRuleTask extends KnockOutRuleAiTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public CheckStatusKnockOutRuleTask() {
        super();
    }

    @Override
    protected void processWithResponse(TaskData taskData, KnockOutRuleResponse packageResponse, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        taskData.setContent(packageResponse);
    }

    @Override
    protected boolean validate(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        SegmentData segmentData = jobData.getTaskData(SegmentUserTask.NAME).getContent();
        UserType userType = segmentData.getUserType();
        if (!serviceObInfo.getUserTypeKnockOutRule().contains(userType)) {
            Log.MAIN.info("ServiceId: {} - agentId - {} userType {} - dose not need to call check Knock out rule, by pass..", jobData.getServiceId(), jobData.getInitiatorId(), userType);
            return false;
        }
        return true;
    }

    @Override
    protected void fillResponseDataWhenReject(OnboardingStatusResponse response, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        ModifyResponseTask.fillUpResponse(jobData, response);
    }

    @Override
    protected boolean isCheckLoanAction(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        return false;
    }

    @Override
    protected void setDeviceInfo(ScreenLog startScreenLog, OnboardingStatusRequest request) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setNfcAvailable(Utils.isNotEmpty(request.getIsNfcAvailable()) ? request.getIsNfcAvailable() : false);
        startScreenLog.setDeviceInfo(deviceInfo);
    }
}
