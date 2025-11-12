package com.mservice.fs.onboarding.job.application.partner.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationPartnerResponse;
import com.mservice.fs.onboarding.model.application.UserProfileInfoWrapper;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;

/**
 * @author phat.duong
 * on 10/16/2024
 **/
public class GetCacheTask extends OnboardingTask<ApplicationPartnerRequest, ApplicationPartnerResponse> {
    public static final TaskName NAME = () -> "GET_DATA_REQUIRED_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public GetCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ApplicationPartnerRequest, ApplicationPartnerResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        String serviceId = onboardingData.getServiceId();
        String applicationId = onboardingData.getRequest().getApplicationId();

        String dataRequiredKey = UserProfileInfoWrapper.createKey(serviceId, applicationId);
        CacheData cacheData = cacheStorage.get(dataRequiredKey);
        if (cacheData != null) {
            Log.MAIN.info("Cache not empty!");
            UserProfileInfoWrapper userProfileInfoWrapper = (UserProfileInfoWrapper) cacheData.getObject();
            taskData.setContent(userProfileInfoWrapper.getUserProfileInfo());
        }
        finish(onboardingData, taskData);
    }
}
