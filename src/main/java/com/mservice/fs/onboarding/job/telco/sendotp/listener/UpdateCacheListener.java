package com.mservice.fs.onboarding.job.telco.sendotp.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.job.telco.GetApplicationTask;
import com.mservice.fs.onboarding.job.telco.GetCacheTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

public class UpdateCacheListener extends OnboardingListener<GenerateOtpRequest, GenerateOtpResponse> {

    public static final String NAME = "ADD_CACHE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public UpdateCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws Throwable {

        ApplicationForm applicationForm = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        if (Utils.isNotEmpty(applicationForm)) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();
            otpInfo.setCurrentTimesGenerate(otpInfo.getCurrentTimesGenerate() + 1);
            otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
            otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());

            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            updateOtpCacheData(jobData, serviceObInfo, applicationData);
        }
    }

    private void updateOtpCacheData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, ServiceObInfo serviceObInfo, ApplicationData applicationData) throws Exception {
        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        CacheData pendingFormCache = jobData.getTaskData(GetCacheTask.NAME).getContent();
        long expiredTimeSaveCache = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);
        pendingFormCache.setExpiredTime(expiredTimeSaveCache);
        Log.MAIN.info("Data add to cache [{}]", JsonUtil.toString(pendingFormCache));
        OnboardingUtils.savePendingForm(cacheStorage, key, pendingFormCache, expiredTimeSaveCache);
    }
}
