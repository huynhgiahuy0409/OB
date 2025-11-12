package com.mservice.fs.onboarding.job.telco.verifyotp;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.job.telco.GetCacheTask;
import com.mservice.fs.onboarding.job.telco.OtpTrackingRequestListener;
import com.mservice.fs.onboarding.job.telco.verifyotp.listener.VerifyOtpUpdateCacheListener;
import com.mservice.fs.onboarding.job.telco.verifyotp.task.BuildResponseTask;
import com.mservice.fs.onboarding.job.telco.verifyotp.task.SendToAITask;
import com.mservice.fs.onboarding.job.telco.verifyotp.task.ValidateTask;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

@Processor(name = OnboardingProcessor.VERIFY_OTP_TELCO)
public class VerifyOtpTelcoJob extends OnboardingJob<VerifyOtpRequest, VerifyOtpResponse> {

    public VerifyOtpTelcoJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<VerifyOtpRequest, VerifyOtpResponse>, VerifyOtpRequest, VerifyOtpResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new GetCacheTask<>(),
                new GetApplicationTask<>(),
                new ValidateTask(),
                new SendToAITask(),
                new BuildResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<VerifyOtpRequest, VerifyOtpResponse>, VerifyOtpRequest, VerifyOtpResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new VerifyOtpUpdateCacheListener(),
                new OtpTrackingRequestListener<>()
        );
    }
}
