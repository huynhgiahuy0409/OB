package com.mservice.fs.onboarding.job.disburse;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.disburse.listener.PushEventChangeStatusToAIListener;
import com.mservice.fs.onboarding.job.disburse.listener.ReCalculateDueDayListener;
import com.mservice.fs.onboarding.job.disburse.listener.SendEventToScoreListener;
import com.mservice.fs.onboarding.job.TrackingPartnerStatusListener;
import com.mservice.fs.onboarding.job.disburse.listener.SendLoanDisbursementLogListener;
import com.mservice.fs.onboarding.job.disburse.listener.SendLoanPartnerResultCodeListener;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Processor(name = OnboardingProcessor.EVENT_DISBURSED)
public class DisburseJob extends OnboardingJob<OnboardingDisburseRequest, OnboardingDisburseResponse> {

    public DisburseJob(String name) {
        super(name);
    }

    private static final Set<ApplicationStatus> allowedStatus = Set.of(ApplicationStatus.ACTIVATED_BY_LENDER);

    @Override
    protected List<Task<OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse>, OnboardingDisburseRequest, OnboardingDisburseResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(new UpdatingStatusTask<>() {
            @Override
            protected boolean allowedStatus(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) {
                return allowedStatus.contains(onboardingData.getRequest().getStatus());
            }
        });
    }

    @Override
    protected List<AbstractListener<OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse>, OnboardingDisburseRequest, OnboardingDisburseResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new ReCalculateDueDayListener(),
                new SendLoanDisbursementLogListener(),
                new SendLoanPartnerResultCodeListener(),
                new TrackingPartnerStatusListener<>(),
                new SendEventToScoreListener()
        );
    }

    @Override
    protected OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> initData(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> data, Base base) throws ReflectiveOperationException, IOException {
        OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> jobData = super.initData(data, base);
        OnboardingDisburseRequest request = jobData.getRequest();
        request.setPhoneNumber(getResource().getPhoneFormat().formatPhone11To10(request.getPhoneNumber()));
        return jobData;
    }

    @Override
    protected boolean parseResultMessage() {
        return false;
    }
}