package com.mservice.fs.onboarding.job.application.step.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.fillform.FillFormRequest;
import com.mservice.fs.onboarding.model.application.fillform.FillFormResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class GetCacheDataTask extends OnboardingTask<FillFormRequest, FillFormResponse> {

    public static final TaskName NAME = () -> "GET_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public GetCacheDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<FillFormRequest, FillFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();
        String key = ApplicationListWrapper.createKey(serviceId, jobData.getInitiatorId());
        CacheData cacheData = cacheStorage.get(key);

        Log.MAIN.info("Get Cache Application with key: {}", key);
        if (cacheData == null) {
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }

        taskData.setContent(cacheData);
        finish(jobData, taskData);
    }


}
