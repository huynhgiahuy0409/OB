package com.mservice.fs.onboarding.job.checkstatus;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.checkstatus.task.*;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.Task;

@Processor(name = {OnboardingProcessor.CHECK_STATUS_QUICK})
public class CheckStatusQuickJob extends CheckStatusJob {

    public CheckStatusQuickJob(String name) {
        super(name);
    }

    @Override
    protected CheckStatusPackageTask getCheckStatusPackageTask() {
        return new CheckStatusQuickPackageTask();
    }
}
