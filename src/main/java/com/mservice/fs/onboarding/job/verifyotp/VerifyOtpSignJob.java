package com.mservice.fs.onboarding.job.verifyotp;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.verifyotp.listener.AddCacheDataListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.NotiUserListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.PartnerResultAIListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.ResponseOtpLogListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.SendPlatformListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.StoreAndSendContractListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.VerifyOtpSaveIdempotencyListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.VerifyOtpTrackingStatusListener;
import com.mservice.fs.onboarding.job.verifyotp.listener.VerifyOtpUpdateStatusListener;
import com.mservice.fs.onboarding.job.verifyotp.task.IdempotencyGetCacheOtpTask;
import com.mservice.fs.onboarding.job.verifyotp.task.VerifyOtpGetUserProfileTask;
import com.mservice.fs.onboarding.job.verifyotp.tasksign.SignModifiedResponseTask;
import com.mservice.fs.onboarding.job.verifyotp.tasksign.SignSendAdapterTask;
import com.mservice.fs.onboarding.job.verifyotp.tasksign.SignValidateTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
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
@Processor(name = {OnboardingProcessor.VERIFY_OTP_SIGN})
public class VerifyOtpSignJob extends OnboardingJob<VerifyOtpRequest, VerifyOtpResponse> {

    public VerifyOtpSignJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<VerifyOtpRequest, VerifyOtpResponse>, VerifyOtpRequest, VerifyOtpResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new LoadDataTask<>(true),
                new IdempotencyGetCacheOtpTask(),
                new ModifiedCacheDataTask<>(),
                new SignValidateTask(),
                new VerifyOtpGetUserProfileTask(),
                new SignSendAdapterTask(),
                new SignModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<VerifyOtpRequest, VerifyOtpResponse>, VerifyOtpRequest, VerifyOtpResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new VerifyOtpSaveIdempotencyListener(),
                new AddCacheDataListener(),
                new SendPlatformListener(),
                new StoreAndSendContractListener(),
                new NotiUserListener(),
                new PartnerResultAIListener<>(),
                new VerifyOtpUpdateStatusListener(),
                new VerifyOtpTrackingStatusListener(),
                new ResponseOtpLogListener()
        );
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData, VerifyOtpResponse response) {
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (OnboardingErrorCode.OTP_VERIFY_LIMIT.getCode().equals(resultCode)) {
            Log.MAIN.info("Verify OTP limit");
            ApplicationForm applicationForm = onboardingData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
            ApplicationData applicationData = applicationForm.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();
            Map<String, Object> templateMap = onboardingData.getTemplateModel();
            OnboardingUtils.putDateToTemplateMap(templateMap, otpInfo.getUnlockOtpTimeInMillis());
        }
        super.addDataBeforeReply(onboardingData, response);
    }
}
