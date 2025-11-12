package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.connection.jdbc.StoreTrackingStatusProcessor;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.trackingstatus.TrackingModel;
import com.mservice.fs.utils.Utils;

public abstract class TrackingRequestListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "TRACKING_STATUS_LISTENER";

    @Autowire(name = "TrackingApplicationStatus")
    private StoreTrackingStatusProcessor<T, R> processor;

    public TrackingRequestListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        if (isActive(jobData)) {
            processor.run(getTrackingStatusModel(jobData), jobData);
        } else {
            Log.MAIN.info("Something wrong!!! Not run tracking status listener");
        }
    }

    protected TrackingModel getTrackingStatusModel(OnboardingData<T, R> jobData) {
        ApplicationForm applicationForm = getApplicationForm(jobData);
        ApplicationData applicationData = applicationForm.getApplicationData();
        TrackingModel trackingStatusModel = new TrackingModel();
        trackingStatusModel.setApplicationId(applicationData.getApplicationId());
        trackingStatusModel.setRawRequest(jobData.getRequest().encode());
        trackingStatusModel.setRawResponse(Utils.cut(jobData.getResponse().encode(), 3000));
        trackingStatusModel.setAgentId(jobData.getInitiatorId());
        trackingStatusModel.setPhoneNumber(jobData.getInitiator());
        trackingStatusModel.setStatus(applicationData.getStatus().name());
        trackingStatusModel.setServiceId(jobData.getServiceId());
        trackingStatusModel.setPartnerId(jobData.getPartnerId());
        trackingStatusModel.setResultCode(jobData.getResponse().getResultCode());
        trackingStatusModel.setProcessName(jobData.getProcessName());
        trackingStatusModel.setTraceId(jobData.getTraceId());
        return trackingStatusModel;
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> jobData);

    protected abstract boolean isActive(OnboardingData<T, R> jobData);

}
