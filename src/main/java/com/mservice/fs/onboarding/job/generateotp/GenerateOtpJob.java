package com.mservice.fs.onboarding.job.generateotp;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.listener.AddCacheDataListener;
import com.mservice.fs.onboarding.job.generateotp.listener.GenerateOtpSendPlatformListener;
import com.mservice.fs.onboarding.job.generateotp.listener.GenerateOtpTrackingRequestListener;
import com.mservice.fs.onboarding.job.generateotp.listener.GenerateOtpUpdateStatusListener;
import com.mservice.fs.onboarding.job.generateotp.listener.RequestOtpLogListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.job.generateotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.job.generateotp.task.ValidateTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
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


/**
 * @author muoi.nong
 */
@Processor(name = {OnboardingProcessor.GENERATE_OTP, OnboardingProcessor.RE_GENERATE_OTP, OnboardingProcessor.GENERATE_OTP_QUICK, OnboardingProcessor.RE_GENERATE_OTP_QUICK})
public class GenerateOtpJob extends OnboardingJob<GenerateOtpRequest, GenerateOtpResponse> {

    public GenerateOtpJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new LoadDataTask<>(true),
                new OtpGetCacheDataTask<>(),
                new ModifiedCacheDataTask<>(),
                new ValidateTask(),
                new SendAdapterTask(),
                new ModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<GenerateOtpRequest, GenerateOtpResponse>, GenerateOtpRequest, GenerateOtpResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new GenerateOtpSendPlatformListener(),
                new AddCacheDataListener(),
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
            ApplicationForm applicationCache = onboardingData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
            ApplicationData applicationData = applicationCache.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();
            Map<String, Object> templateMap = onboardingData.getTemplateModel();
            OnboardingUtils.putDateToTemplateMap(templateMap, otpInfo.getUnlockOtpTimeInMillis());
        }
        super.addDataBeforeReply(onboardingData, response);
    }

}
