package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.verifyotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.Set;

public class AddCacheDataListener extends OnboardingListener<VerifyOtpRequest, VerifyOtpResponse> {

    public static final String NAME = "ADD_CACHE_DATA";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final Set<ApplicationStatus> REMOVED_STATUS = Set.of(ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    public AddCacheDataListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws Throwable {
        CacheData pendingFormCache = jobData.getTaskData(OtpGetCacheDataTask.NAME).getContent();
        if (Utils.isNotEmpty(pendingFormCache)) {
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            updateOtpCacheData(jobData, serviceObInfo, pendingFormCache);
        }
    }

    private void updateOtpCacheData(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, ServiceObInfo serviceObInfo, CacheData pendingFormCache) throws Exception {
        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        pendingFormCache.setExpiredTime(serviceObInfo.getPendingFormCacheTimeInMillis());
        Log.MAIN.info("Data add to cache [{}]", JsonUtil.toString(pendingFormCache));
        VerifyOtpAdapterResponse adapterResponse = jobData.getTaskData(SendAdapterTask.NAME).getContent();

        if (CommonErrorCode.SUCCESS.getCode().equals(jobData.getResponse().getResultCode())
                || REMOVED_STATUS.contains(jobData.getResponse().getApplicationData().getStatus())
                || (Utils.isNotEmpty(adapterResponse) && adapterResponse.isClearPendingForm())) {
            Log.MAIN.info("Remove cache Application with key {}", key);
            cacheStorage.remove(key);
        } else {
            ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
            if (serviceObInfo.isMatchAction(Action.REMIND_PENDING_FORM, jobData.getProcessName())) {
                OnboardingUtils.updateCacheForNotify(jobData, serviceObInfo, applicationForm, cacheStorage);
            }
            OnboardingUtils.savePendingForm(cacheStorage, key, pendingFormCache, OnboardingUtils.calculatePendingFormExpireTime(applicationForm.getApplicationData().getExpiredTimeInMillis(), serviceObInfo));
        }
    }
}
