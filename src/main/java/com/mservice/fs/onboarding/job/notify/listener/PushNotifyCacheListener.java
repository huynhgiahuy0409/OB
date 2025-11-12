package com.mservice.fs.onboarding.job.notify.listener;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.notify.task.SendPlatformTask;
import com.mservice.fs.onboarding.model.OnboardingNotifyResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserRequest;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.Utils;

public class PushNotifyCacheListener extends AbstractListener<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig> {

    private static final String NAME = "PUT_CACHE_NOTI";

    @Autowire(name = "CacheStorageRemind")
    private RedisCacheStorage cacheStorageRemind;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public PushNotifyCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(PlatformData<NotifyUserRequest, NotifyUserResponse> onboardingData) throws Throwable {
        Integer resultCode = onboardingData.getResponse().getResultCode();
        OnboardingNotifyResponse notifyResponse = onboardingData.getTaskData(SendPlatformTask.NAME).getContent();
        if (!OnboardingErrorCode.CACHE_NOT_FOUND.getCode().equals(onboardingData.getResponse().getResultCode())
                && Utils.isNotEmpty(notifyResponse.getTimeRemindInMillis())
                && notifyResponse.getTimeRemindInMillis() > 0) {
            Log.MAIN.info("Cache Notify user with resultCode: {} in {}", resultCode, notifyResponse.getTimeRemindInMillis());
            NotifyUserRequest request = onboardingData.getRequest();
            ApplicationListWrapper applicationListWrapper = new ApplicationListWrapper();
            applicationListWrapper.setApplicationForms(request.getApplicationForms());
            OnboardingUtils.updateCacheForNotify(onboardingData, notifyResponse.getTimeRemindInMillis(), applicationListWrapper, cacheStorageRemind);
        } else {
            Log.MAIN.info("Not cache Notify user with resultCode: {}", resultCode);
        }
    }


}
