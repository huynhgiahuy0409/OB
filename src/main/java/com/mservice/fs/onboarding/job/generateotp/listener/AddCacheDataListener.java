package com.mservice.fs.onboarding.job.generateotp.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

public class AddCacheDataListener extends OnboardingListener<GenerateOtpRequest, GenerateOtpResponse> {

    public static final String NAME = "ADD_CACHE_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public AddCacheDataListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws Throwable {

        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        if (Utils.isNotEmpty(applicationForm)) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();

            GenerateOtpAdapterResponse adapterResponse = jobData.getTaskData(SendAdapterTask.NAME).getContent();

            if (Utils.isNotEmpty(adapterResponse)) {
                otpInfo.setOtpPartnerKey(adapterResponse.getOtpPartnerKey());
            }

            otpInfo.setCurrentTimesGenerate(otpInfo.getCurrentTimesGenerate() + 1);
            otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
            otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());

            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            updateOtpCacheData(jobData, serviceObInfo, applicationData);

            if (serviceObInfo.isMatchAction(Action.REMIND_PENDING_FORM, jobData.getProcessName())) {
                OnboardingUtils.updateCacheForNotify(jobData, serviceObInfo, applicationForm, cacheStorage);
            }
        }
    }

    private void updateOtpCacheData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, ServiceObInfo serviceObInfo, ApplicationData applicationData) throws Exception {
        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        CacheData pendingFormCache = jobData.getTaskData(OtpGetCacheDataTask.NAME).getContent();
        long expiredTimeSaveCache = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);
        pendingFormCache.setExpiredTime(expiredTimeSaveCache);
        Log.MAIN.info("Data add to cache [{}]", JsonUtil.toString(pendingFormCache));
        OnboardingUtils.savePendingForm(cacheStorage, key, pendingFormCache, expiredTimeSaveCache);
    }
}
