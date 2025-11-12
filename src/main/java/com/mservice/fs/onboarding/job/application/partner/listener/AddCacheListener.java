package com.mservice.fs.onboarding.job.application.partner.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.onboarding.model.application.UserProfileInfoWrapper;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
public class AddCacheListener extends OnboardingListener<ApplicationPartnerRequest, ApplicationPartnerResponse> {

    public static final String NAME = "ADD_CACHE_USER_PROFILE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public AddCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse> onboardingData) throws Throwable {
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (!CommonErrorCode.SUCCESS.getCode().equals(resultCode)) {
            Log.MAIN.info("Response not success - Skip cache user profile");
            return;
        }

        UserProfileInfo userProfileInfo = onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        if (userProfileInfo == null) {
            Log.MAIN.info("Not call User Profile - Skip cache user profile");
            return;
        }
        UserProfileInfoWrapper userProfileInfoWrapper = new UserProfileInfoWrapper();
        userProfileInfoWrapper.setUserProfileInfo(userProfileInfo);
        Long timeOut = UserProfileInfoWrapper.TIME_SAVE_USER_PROFILE;
        String serviceId = onboardingData.getServiceId();

        if (Utils.isNotEmpty(getConfig().getCacheUserProfileMap()) && Utils.isNotEmpty(getConfig().getCacheUserProfileMap().get(onboardingData.getServiceId()))) {
            timeOut = getConfig().getCacheUserProfileMap().get(onboardingData.getServiceId());
        }
        CacheData userProfileCache = new CacheData(onboardingData.getTraceId(), timeOut, userProfileInfoWrapper);
        String key = UserProfileInfoWrapper.createKey(serviceId, onboardingData.getRequest().getApplicationId());
        cacheStorage.put(key, userProfileCache);

    }
}
