package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.LendingConfiguredPackageTask;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

public class CheckStatusLendingPackageTask extends LendingConfiguredPackageTask<OnboardingStatusRequest, OnboardingStatusResponse> {
    @Override
    protected ServiceObInfo getServiceObInfo(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, DataService<ServiceObConfig> onboardingDataInfo) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
    }
}
