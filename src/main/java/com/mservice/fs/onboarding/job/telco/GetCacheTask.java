package com.mservice.fs.onboarding.job.telco;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OtpRequest;
import com.mservice.fs.onboarding.model.OtpResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.Utils;

public class GetCacheTask<T extends OtpRequest, R extends OtpResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_CACHE";
    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public GetCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        Log.MAIN.info("key: " + key);
        CacheData pendingFormCache = cacheStorage.get(key);
        if (Utils.isEmpty(pendingFormCache)) {
            Log.MAIN.error("Cache not found with key {}!!!", key);
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }
        taskData.setContent(pendingFormCache);
        finish(jobData, taskData);
    }
}

