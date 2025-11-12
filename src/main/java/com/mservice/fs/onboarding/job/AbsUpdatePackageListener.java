package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.UpdatePackageProcessor;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/14/2023
 */
public abstract class AbsUpdatePackageListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    @Autowire(name = "UpdatePackage")
    private UpdatePackageProcessor updatePackageProcessor;

    public AbsUpdatePackageListener(String name) {
        super(name);
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        Integer resultCode = jobData.getResponse().getResultCode();
        PackageInfo chosenPackage = getChosenPackage(jobData);
        String applicationId = getApplicationId(jobData);

        if (CommonErrorCode.SUCCESS.getCode().equals(resultCode) && Utils.isNotEmpty(applicationId) && chosenPackage != null) {
            Log.MAIN.info("DB Error when update package with applicationId {} - loanAmount {}, packageCode {}", applicationId, chosenPackage.getLoanAmount(), chosenPackage.getPackageCode());
            updatePackageProcessor.execute(chosenPackage, applicationId);
        }
    }

    public abstract String getApplicationId(OnboardingData<T, R> jobData);

    public abstract PackageInfo getChosenPackage(OnboardingData<T, R> jobData);

}
