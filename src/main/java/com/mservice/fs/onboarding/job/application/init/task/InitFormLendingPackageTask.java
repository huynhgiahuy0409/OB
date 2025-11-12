package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.job.LendingConfiguredPackageTask;

public class InitFormLendingPackageTask extends LendingConfiguredPackageTask<InitFormRequest, InitFormResponse> {
    @Override
    protected boolean isAsync() {
        return true;
    }
}
