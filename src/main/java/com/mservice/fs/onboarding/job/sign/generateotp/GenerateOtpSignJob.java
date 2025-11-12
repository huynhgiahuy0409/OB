package com.mservice.fs.onboarding.job.sign.generateotp;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.sign.generateotp.listener.AddCacheDataListener;
import com.mservice.fs.onboarding.job.sign.generateotp.listener.GenerateOtpSendApplicationDataPlatformListener;
import com.mservice.fs.onboarding.job.sign.generateotp.listener.GenerateOtpTrackingRequestListener;
import com.mservice.fs.onboarding.job.sign.generateotp.listener.GenerateOtpUpdateStatusListener;
import com.mservice.fs.onboarding.job.sign.generateotp.listener.RequestOtpLogListener;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.OtpGetUserProfileTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ValidateTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Processor(name = {OnboardingProcessor.GENERATE_OTP_SIGN, OnboardingProcessor.RE_GENERATE_OTP_SIGN})
public class GenerateOtpSignJob extends OnboardingJob<GenerateOtpRequest, GenerateOtpResponse> {

    public GenerateOtpSignJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new LoadDataTask<>(true),
                new OtpGetCacheDataTask<>(),
                new ModifiedCacheDataTask<>(),
                new GetApplicationTask(),
                new ValidateTask(),
                new OtpGetUserProfileTask(),
                new SendAdapterTask(),
                new ModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new AddCacheDataListener(),
                new GenerateOtpSendApplicationDataPlatformListener(),
                new GenerateOtpUpdateStatusListener(),
                new GenerateOtpTrackingRequestListener(),
                new RequestOtpLogListener()
        );
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData, GenerateOtpResponse response) {
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (OnboardingErrorCode.OTP_GENERATE_LIMIT.getCode().equals(resultCode)) {
            Log.MAIN.info("Generate OTP limit");
            ApplicationData applicationData = onboardingData.getTaskData(GetApplicationTask.NAME).getContent();
            OtpInfo otpInfo = applicationData.getOtpInfo();
            Map<String, Object> templateMap = onboardingData.getTemplateModel();
            OnboardingUtils.putDateToTemplateMap(templateMap, otpInfo.getUnlockOtpTimeInMillis());
        }
        super.addDataBeforeReply(onboardingData, response);
    }

}
