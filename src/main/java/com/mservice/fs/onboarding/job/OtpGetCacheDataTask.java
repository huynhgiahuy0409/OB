package com.mservice.fs.onboarding.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.OtpRequest;
import com.mservice.fs.onboarding.model.OtpResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;

public class OtpGetCacheDataTask<T extends OtpRequest, R extends OtpResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_CACHE_DATA";
    @Autowire(name = "CacheStorage")
    protected RedisCacheStorage cacheStorage;

    public OtpGetCacheDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        getApplicationCacheData(taskData, jobData);
        finish(jobData, taskData);
    }

    private void getApplicationCacheData(TaskData taskData, OnboardingData<T, R> jobData) throws JsonProcessingException, BaseException {

        String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        Log.MAIN.info("key: " + key);
        CacheData pendingFormCache = cacheStorage.get(key);

        taskData.setContent(pendingFormCache);
    }
}
