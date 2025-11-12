package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;

/**
 * @author muoi.nong
 */
public class CheckStatusGetDataDeDupDBTask extends GetDataDeDupDBTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    @Override
    protected boolean isAsync() {
        return true;
    }
}
