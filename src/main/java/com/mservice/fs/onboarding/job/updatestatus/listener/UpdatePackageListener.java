package com.mservice.fs.onboarding.job.updatestatus.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.onboarding.connection.jdbc.UpdatePackageProcessor;
import com.mservice.fs.onboarding.job.AbsUpdatePackageListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;

/**
 * @author hoang.thai
 * on 12/14/2023
 */
public class UpdatePackageListener extends AbsUpdatePackageListener<UpdatingStatusRequest, UpdatingStatusResponse> {

    private static final String NAME = "UPDATE_PACKAGE_UPDATE_STATUS";

    public UpdatePackageListener() {
        super(NAME);
    }


    @Override
    public String getApplicationId(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> jobData) {
        return jobData.getRequest().getApplicationId();
    }

    @Override
    public PackageInfo getChosenPackage(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> jobData) {
        return jobData.getRequest().getChosenPackage();
    }
}
