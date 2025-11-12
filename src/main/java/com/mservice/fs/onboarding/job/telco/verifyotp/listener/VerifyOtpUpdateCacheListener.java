package com.mservice.fs.onboarding.job.telco.verifyotp.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.job.telco.GetCacheTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.redis.service.RedisCacheStorage;

import java.util.Set;

public class VerifyOtpUpdateCacheListener extends OnboardingListener<VerifyOtpRequest, VerifyOtpResponse> {

    public static final String NAME = "ADD_CACHE_DATA";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private final Set<ErrorCode> ERROR_CODE_SAVE_PENDING_FORM = Set.of(CommonErrorCode.SUCCESS, OnboardingErrorCode.OTP_VERIFY_LIMIT, OnboardingErrorCode.OTP_VERIFY_ERROR);

    public VerifyOtpUpdateCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws Throwable {

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        CacheData pendingFormCache = jobData.getTaskData(GetCacheTask.NAME).getContent();
        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        pendingFormCache.setExpiredTime(serviceObInfo.getPendingFormCacheTimeInMillis());

        if (ERROR_CODE_SAVE_PENDING_FORM.contains(jobData.getResponse().getResult())) {
            if (CommonErrorCode.SUCCESS != jobData.getResponse().getResult()) {
                ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
                ApplicationData applicationData = applicationForm.getApplicationData();
                OtpInfo otpInfo = applicationData.getOtpInfo();
                otpInfo.setCurrentTimesVerify(otpInfo.getCurrentTimesVerify() + 1);
                otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
                otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());

            }
            cacheStorage.update(key, pendingFormCache);
            Log.MAIN.info("Update application cache success: {}", pendingFormCache.getObject());
        }
    }
}
