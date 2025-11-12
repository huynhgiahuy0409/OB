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
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.ConfirmModifyResponseTask;
import com.mservice.fs.onboarding.job.application.submit.task.PaymentInfoTask;
import com.mservice.fs.onboarding.job.application.submit.task.SocialSellerDataTask;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
//@Processor(name = {OnboardingProcessor.CONFIRM_KYC_FMOB, OnboardingProcessor.CONFIRM_FACE_MATCHING_FMOB})
public class ConfirmActionFMOBJob extends OnboardingJob<ConfirmRequest, ConfirmResponse> {

    public ConfirmActionFMOBJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<ConfirmRequest, ConfirmResponse>, ConfirmRequest, ConfirmResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new SubmitGetUserProfileTask<>(),
                new GetCacheTask<>(),
                new ApplicationTask<>(),
                new CheckLoanDeciderTask<>(),
                new SocialSellerDataTask<>(),
                new PaymentInfoTask<>(),
                new FMOBSendAdapterTask<>(),
                new ConfirmModifyResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<ConfirmRequest, ConfirmResponse>, ConfirmRequest, ConfirmResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener<>(),
                new StoreApplicationDataListener<>(),
                new SumitFormTrackingRequestListener<>(),
                new NotiUserListener<>(),
                new PartnerResultLogListener<>()
        );
    }
}
