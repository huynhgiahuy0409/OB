package com.mservice.fs.onboarding.job.reasonmessage.listener;

import com.mservice.fs.onboarding.job.AbsUpdatePackageListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageRequest;
import com.mservice.fs.onboarding.model.api.reasonmessage.ReasonMessageResponse;

public class UpdatePackageListener extends AbsUpdatePackageListener<ReasonMessageRequest, ReasonMessageResponse> {

    private static final String NAME = "UPDATE_PACKAGE_UPDATE_REASONMESSAGE";

    public UpdatePackageListener() {
        super(NAME);
    }

    @Override
    public String getApplicationId(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> jobData) {
        return jobData.getRequest().getApplicationId();
    }

    @Override
    public PackageInfo getChosenPackage(OnboardingData<ReasonMessageRequest, ReasonMessageResponse> jobData) {
        return jobData.getRequest().getChosenPackage();
    }
}
