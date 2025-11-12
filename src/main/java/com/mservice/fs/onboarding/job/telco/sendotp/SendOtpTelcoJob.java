package com.mservice.fs.onboarding.job.telco.sendotp;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.job.telco.GetCacheTask;
import com.mservice.fs.onboarding.job.telco.OtpTrackingRequestListener;
import com.mservice.fs.onboarding.job.telco.sendotp.listener.UpdateCacheListener;
import com.mservice.fs.onboarding.job.telco.sendotp.task.BuildResponseTask;
import com.mservice.fs.onboarding.job.telco.sendotp.task.SendToAITask;
import com.mservice.fs.onboarding.job.telco.sendotp.task.ValidateTask;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.List;

@Processor(name = {OnboardingProcessor.GENERATE_OTP_TELCO, OnboardingProcessor.RE_GENERATE_OTP_TELCO})
public class SendOtpTelcoJob extends OnboardingJob<GenerateOtpRequest, GenerateOtpResponse> {

    public SendOtpTelcoJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getTaskList() throws Exception {
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
    protected List<AbstractListener<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener(),
                new OtpTrackingRequestListener<>()
        );
    }
}
