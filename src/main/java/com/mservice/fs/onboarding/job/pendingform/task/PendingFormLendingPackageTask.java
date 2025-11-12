package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.onboarding.job.LendingConfiguredPackageTask;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;

public class PendingFormLendingPackageTask extends LendingConfiguredPackageTask<PendingFormRequest, PendingFormResponse> {
    @Override
    protected boolean isAsync() {
        return true;
    }
}
