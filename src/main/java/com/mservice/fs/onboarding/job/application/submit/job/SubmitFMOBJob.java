package com.mservice.fs.onboarding.job.application.submit.job;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.submit.CheckLoanDeciderTask;
import com.mservice.fs.onboarding.job.application.submit.FMOBSendAdapterTask;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.job.application.submit.listener.NotiUserListener;
import com.mservice.fs.onboarding.job.application.submit.listener.PartnerResultLogListener;
import com.mservice.fs.onboarding.job.application.submit.listener.StoreApplicationDataListener;
import com.mservice.fs.onboarding.job.application.submit.listener.SumitFormTrackingRequestListener;
import com.mservice.fs.onboarding.job.application.submit.listener.UpdateCacheListener;
import com.mservice.fs.onboarding.job.application.submit.task.PaymentInfoTask;
import com.mservice.fs.onboarding.job.application.submit.task.SocialSellerDataTask;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitFMOBModifyResponse;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.job.application.submit.task.UpdateApplicationTask;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
//@Processor(name = OnboardingProcessor.FINAL_SUBMIT_FMOB)
public class SubmitFMOBJob extends OnboardingJob<SubmitRequest, SubmitResponse> {

    public SubmitFMOBJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new SubmitGetUserProfileTask(),
                new GetCacheTask<>(),
                new UpdateApplicationTask(),
                new CheckLoanDeciderTask<>(),
                new SocialSellerDataTask<>(),
                new PaymentInfoTask<>(),
                new FMOBSendAdapterTask<>(),
                new SubmitFMOBModifyResponse()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener<>(),
                new StoreApplicationDataListener<>(),
                new SumitFormTrackingRequestListener<>(),
                new NotiUserListener<>(),
                new PartnerResultLogListener<>()
        );
    }
}
