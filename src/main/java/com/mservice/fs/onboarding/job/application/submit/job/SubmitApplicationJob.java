package com.mservice.fs.onboarding.job.application.submit.job;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.job.application.submit.SubmitSendAdapterTask;
import com.mservice.fs.onboarding.job.application.submit.listener.*;
import com.mservice.fs.onboarding.job.application.submit.task.*;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckDeDupMomoTask;
import com.mservice.fs.onboarding.job.checkstatus.task.GetDataDeDupDBTask;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Processor(name = {OnboardingProcessor.FINAL_SUBMIT, OnboardingProcessor.FINAL_SUBMIT_QUICK})
public class SubmitApplicationJob extends OnboardingJob<SubmitRequest, SubmitResponse> {

    public SubmitApplicationJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new SubmitGetUserProfileTask<>(),
                new GetDataDeDupDBTask<>(),
                new CheckDeDupMomoTask<>(),
                new GetCacheTask<>(),
                new UpdateApplicationTask(),
                new FinalSubmitLoanDeciderTask(),
                new ExtraActions<>(),
                new SubmitSendPlatformTask(),
                new SubmitSendAdapterTask<>(),
                new SubmitModifyResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener<>(),
                new SubmitFormSendApplicationDataPlatformListener(),
                new StoreApplicationDataListener<>(),
                new SumitFormTrackingRequestListener<>(),
                new NotiUserListener<>(),
                new PartnerResultLogListener<>(),
                new KorApproveStartScreenListener<>(),
                new SummaryScreenListener(),
                new RequestOtpLogListener<>(),
                new SendUserConsent(),
                new SendAdapterLoanDeciderRejectListener<>(),
                new AlertMissingInfoUserProfileListenner<>()
        );
    }

}
