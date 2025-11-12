package com.mservice.fs.onboarding.job;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.Utils;

import java.util.Map;

/**
 * @author phat.duong
 * on 1/15/2025
 **/
public class SaveCacheListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "ADD_CACHE_IDEMPOTENCY";
    public static final long EXPIRED_TIME = 120000L;
    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public SaveCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> onboardingData) throws Throwable {
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (!CommonErrorCode.SUCCESS.getCode().equals(resultCode)) {
            Log.MAIN.info("Response not success - Skip cache");
            return;
        }
        if (onboardingData.isLoadFromCache()) {
            Log.MAIN.info("Response from cache - skip save cache");
            return;
        }

        Map<String, Long> cacheProcessNameMap = getConfig().getCacheProcessNameMap();
        Long timeOut = EXPIRED_TIME;
        if (Utils.isNotEmpty(cacheProcessNameMap) && Utils.isNotEmpty(cacheProcessNameMap.get(onboardingData.getProcessName()))) {
            timeOut = cacheProcessNameMap.get(onboardingData.getProcessName());
        }

        R response = onboardingData.getResponse();
        CacheData cacheData = new CacheData(onboardingData.getTraceId(), timeOut, response);
        cacheStorage.put(getKey(onboardingData), cacheData);
        Log.MAIN.info("Store cache success!");
    }

    protected String getKey(OnboardingData<T, R> onboardingData) {
        return onboardingData.getProcessName() + ":" + onboardingData.getServiceId() + "_" + onboardingData.getInitiatorId();
    }
}
