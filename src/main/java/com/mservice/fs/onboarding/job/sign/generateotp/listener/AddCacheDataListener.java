package com.mservice.fs.onboarding.job.sign.generateotp.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.GetApplicationTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.sign.generateotp.task.SendAdapterTask;
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

import java.util.List;

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
        CacheData pendingFormCache = jobData.getTaskData(OtpGetCacheDataTask.NAME).getContent();
        ApplicationData applicationDataSign = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        GenerateOtpAdapterResponse adapterResponse = jobData.getTaskData(SendAdapterTask.NAME).getContent();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        if (Utils.isEmpty(applicationDataSign)) {
            return;
        }

        if (Utils.isNotEmpty(adapterResponse) && adapterResponse.isClearPendingForm()) {
            String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            cacheStorage.remove(key);
            return;
        }

        if (!serviceObInfo.isMatchAction(Action.UPDATE_WHEN_FAIL, jobData.getProcessName()) && !CommonErrorCode.SUCCESS.getCode().equals(jobData.getResponse().getResultCode())) {
            return;
        }

        if (Utils.isEmpty(applicationForm) && Utils.isNotEmpty(pendingFormCache)) {

            ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) pendingFormCache.getObject();
            buildApplicationForm(applicationListWrapper, jobData, applicationDataSign, serviceObInfo);

        } else if (Utils.isEmpty(applicationForm) && Utils.isEmpty(pendingFormCache)) {
            ApplicationListWrapper applicationListWrapper = new ApplicationListWrapper();
            buildApplicationForm(applicationListWrapper, jobData, applicationDataSign, serviceObInfo);
            pendingFormCache = new CacheData();
            pendingFormCache.setCacheObject(applicationListWrapper);
        }
        updateOtpCacheData(jobData, pendingFormCache, serviceObInfo, applicationDataSign);

    }

    private void buildApplicationForm(ApplicationListWrapper applicationListWrapper, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, ApplicationData applicationData, ServiceObInfo serviceObInfo) {

        applicationData.setExpiredTimeInMillis(System.currentTimeMillis() + serviceObInfo.getPendingFormCacheTimeInMillis());

        List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms();
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setApplicationData(applicationData);
        applicationForm.setRedirectTo(serviceObInfo.getNextDirection(jobData.getProcessName()));
        applicationForms.add(applicationForm);
    }

    private void updateOtpCacheData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, CacheData pendingFormCache, ServiceObInfo serviceObInfo, ApplicationData applicationData) throws Exception {
        OtpInfo otpInfo = applicationData.getOtpInfo();

        GenerateOtpAdapterResponse adapterResponse = jobData.getTaskData(SendAdapterTask.NAME).getContent();

        if (Utils.isNotEmpty(adapterResponse)) {
            otpInfo.setOtpPartnerKey(adapterResponse.getOtpPartnerKey());
        }

        otpInfo.setCurrentTimesGenerate(otpInfo.getCurrentTimesGenerate() + 1);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
        otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());

        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        long expiredTimeSaveCache = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);
        pendingFormCache.setExpiredTime(expiredTimeSaveCache);
        Log.MAIN.info("Data add to cache [{}]", JsonUtil.toString(pendingFormCache));
        OnboardingUtils.savePendingForm(cacheStorage, key, pendingFormCache, expiredTimeSaveCache);
    }
}
