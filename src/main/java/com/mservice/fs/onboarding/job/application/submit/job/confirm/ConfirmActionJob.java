package com.mservice.fs.onboarding.job.application.submit.job.confirm;

import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.job.application.submit.SubmitSendAdapterTask;
import com.mservice.fs.onboarding.job.application.submit.listener.KorApproveStartScreenListener;
import com.mservice.fs.onboarding.job.application.submit.listener.NotiUserListener;
import com.mservice.fs.onboarding.job.application.submit.listener.PartnerResultLogListener;
import com.mservice.fs.onboarding.job.application.submit.listener.RequestOtpLogListener;
import com.mservice.fs.onboarding.job.application.submit.listener.SendAdapterLoanDeciderRejectListener;
import com.mservice.fs.onboarding.job.application.submit.listener.StoreApplicationDataListener;
import com.mservice.fs.onboarding.job.application.submit.listener.SumitFormTrackingRequestListener;
import com.mservice.fs.onboarding.job.application.submit.listener.UpdateCacheListener;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.ConfirmModifyResponseTask;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmResponse;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
public abstract class ConfirmActionJob<T extends ConfirmRequest, R extends ConfirmResponse> extends OnboardingJob<T, R> {

    public ConfirmActionJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<T, R>, T, R, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new SubmitGetUserProfileTask<>(),
                new GetCacheTask<>(),
                new ApplicationTask<>(),
                getValidateTask(),
                getCheckLoanDeciderTask(),
                new SubmitSendAdapterTask<>(),
                new ConfirmModifyResponseTask<>()
        );
    }

    protected abstract Task<OnboardingData<T, R>, T, R, OnboardingConfig> getValidateTask();

    protected abstract Task<OnboardingData<T, R>, T, R, OnboardingConfig> getCheckLoanDeciderTask();

    @Override
    protected List<AbstractListener<OnboardingData<T, R>, T, R, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener<>(),
                new StoreApplicationDataListener<>(),
                new NotiUserListener<>(),
                new SumitFormTrackingRequestListener<>(),
                new PartnerResultLogListener<>(),
                new RequestOtpLogListener<>(),
                new KorApproveStartScreenListener<>(),
                new SendAdapterLoanDeciderRejectListener<>()
        );
    }
}
