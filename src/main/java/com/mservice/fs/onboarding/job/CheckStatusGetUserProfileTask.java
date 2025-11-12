package com.mservice.fs.onboarding.job;

import com.mservice.fs.onboarding.job.checkstatus.task.ServiceObInfoTask;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import lombok.SneakyThrows;

/**
 * @author hoang.thai
 * on 12/14/2023
 */
public class CheckStatusGetUserProfileTask<T extends OnboardingRequest, R extends OnboardingResponse> extends AbsGetUserProfileTask<T, R> {

    @SneakyThrows
    @Override
    protected boolean isValidCondition(OnboardingData<T, R> jobData, TaskData taskData) {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.GET_USER_PROFILE, jobData.getProcessName())) {
            return true;
        }
        return false;
    }
}
