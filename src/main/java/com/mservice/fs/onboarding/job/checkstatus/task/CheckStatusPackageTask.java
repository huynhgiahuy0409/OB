package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.PackageAiTask;
import com.mservice.fs.onboarding.job.application.init.task.InitFormCheckKnockOutRuleTask;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.SegmentData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author hoang.thai on 8/28/2023
 */
public class CheckStatusPackageTask extends PackageAiTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    @Override
    protected boolean validate(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        SegmentData segmentData = jobData.getTaskData(SegmentUserTask.NAME).getContent();
        UserType userType = segmentData.getUserType();
        if (!serviceObInfo.getUserTypeGetPackage().contains(userType)) {
            Log.MAIN.info("ServiceId: {} - agentId - {} userType {} - dose not need to call getPackage, by pass..", jobData.getServiceId(), jobData.getInitiatorId(), userType);
            return false;
        }
        return true;
    }

    @Override
    protected String getSegmentUser(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        TaskData taskData = jobData.getTaskData(CheckStatusKnockOutRuleTask.NAME);
        if (Utils.isNotEmpty(taskData) && Utils.isNotEmpty(taskData.getContent())) {
            KnockOutRuleResponse packageResponse = jobData.getTaskData(CheckStatusKnockOutRuleTask.NAME).getContent();
            return packageResponse.getLoanDeciderRecord().getProfile().getSegmentUser();
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void processWithBadRequest(GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) throws BaseException {
        //Do nothing
        //CLO does not call get package in api check-status
        //CCM call get Package in check Status with UserType New and Reject and when user not found return package not throw Exception

    }

}
