package com.mservice.fs.onboarding.job.updatestatus;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.TrackingPartnerStatusListener;
import com.mservice.fs.onboarding.job.updatestatus.listener.PushEventAIListener;
import com.mservice.fs.onboarding.job.updatestatus.listener.PushOfferPackageAIListener;
import com.mservice.fs.onboarding.job.updatestatus.listener.SendPlatformListener;
import com.mservice.fs.onboarding.job.updatestatus.listener.UpdatePackageListener;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.util.List;
import java.util.Set;

@Processor(name = OnboardingProcessor.UPDATE_STATUS)
public class UpdateStatusJob extends OnboardingJob<UpdatingStatusRequest, UpdatingStatusResponse> {

    public UpdateStatusJob(String name) {
        super(name);
    }

    private static final Set<ApplicationStatus> allowedStatus = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.APPROVED_BY_LENDER, ApplicationStatus.ACTIVATED_BY_LENDER, ApplicationStatus.LIQUIDATION, ApplicationStatus.REVIEW_BY_LENDER);

    @Override
    protected List<Task<OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse>, UpdatingStatusRequest, UpdatingStatusResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(new UpdatingStatusTask<>() {
            @Override
            protected boolean allowedStatus(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
                return allowedStatus.contains(onboardingData.getRequest().getStatus());
            }
        });
    }

    @Override
    protected List<AbstractListener<OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse>, UpdatingStatusRequest, UpdatingStatusResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new PushEventAIListener(),
                new UpdatePackageListener(),
                new TrackingPartnerStatusListener<>(),
                new PushOfferPackageAIListener(),
                new SendPlatformListener()
        );
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}
