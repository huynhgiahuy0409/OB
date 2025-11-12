package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.connection.jdbc.UpdateApplicationStatusProcessor;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.utils.CommonConstant;

public abstract class UpdateApplicationStatusListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "UPDATE_STATUS_LISTENER";

    public UpdateApplicationStatusListener() {
        super(NAME);
    }

    @Autowire(name = "UpdateApplicationStatus")
    private UpdateApplicationStatusProcessor<T, R> processor;

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        if (isActive(jobData)) {
            processor.run(jobData, getApplicationId(jobData), getStatus(jobData), getPartnerApplicationId(jobData), getReasonId(jobData), getReasonMessage(jobData));
        } else {
            Log.MAIN.info("Something wrong!!! Not update status listener");
        }
    }

    protected String getPartnerApplicationId(OnboardingData<T, R> jobData) {
        return null;
    }

    protected abstract String getApplicationId(OnboardingData<T, R> jobData);

    protected abstract ApplicationStatus getStatus(OnboardingData<T, R> jobData);

    protected int getReasonId(OnboardingData<T, R> jobData) {
        return 0;
    }

    protected String getReasonMessage(OnboardingData<T, R> jobData) {
        return CommonConstant.STRING_EMPTY;
    }

    protected abstract boolean isActive(OnboardingData<T, R> jobData);
}
