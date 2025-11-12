package com.mservice.fs.onboarding.job.checkstatus.task;


import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

public class CheckStatusQuickPackageTask extends CheckStatusPackageTask {

    @Override
    protected boolean validate(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        return true;
    }

    @Override
    protected PackageCache processPackage200(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, TaskData taskData, GetPackageResponse packageAiResponse, Integer responseAiCode) throws BaseException, ValidatorException, Exception {
        return null;
    }
}
