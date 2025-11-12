package com.mservice.fs.onboarding.job.application.submit;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.UserProfileInfoWrapper;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.util.Map;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class GetCacheTask<T extends ConfirmRequest, R extends SubmitResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_CACHE_APPLICATION";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public GetCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();
        String key = ApplicationListWrapper.createKey(serviceId, jobData.getInitiatorId());
        CacheData cacheData = cacheStorage.get(key);
        Log.MAIN.info("Get Cache Application with key: {}", key);
        if (cacheData == null) {
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }
        Log.MAIN.info("CacheData: [{}]", cacheData.getObject());

        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        if (userProfileInfo == null) {
            Log.MAIN.info("Not call User Profile - Skip cache user profile");
            taskData.setContent(cacheData);
            finish(jobData, taskData);
            return;
        }

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        Action actionCheckUserProfile = Action.CHECK_USER_PROFILE;
        if (!serviceObInfo.isMatchAction(actionCheckUserProfile, jobData.getProcessName())) {
            Log.MAIN.info("Not check User Profile - Skip cache user profile");
            taskData.setContent(cacheData);
            finish(jobData, taskData);
            return;
        }

        UserProfileInfoWrapper userProfileInfoWrapper = new UserProfileInfoWrapper();
        userProfileInfoWrapper.setUserProfileInfo(userProfileInfo);
        Long timeOut = UserProfileInfoWrapper.TIME_SAVE_USER_PROFILE;

        Map<String, Long> cacheUserProfileMap = getConfig().getCacheUserProfileMap();
        if (Utils.isNotEmpty(cacheUserProfileMap) && Utils.isNotEmpty(cacheUserProfileMap.get(jobData.getServiceId()))) {
            timeOut = cacheUserProfileMap.get(jobData.getServiceId());
        }
        CacheData userProfileCache = new CacheData(jobData.getTraceId(), timeOut, userProfileInfoWrapper);
        String userProfileKey = UserProfileInfoWrapper.createKey(serviceId, jobData.getRequest().getApplicationId());
        cacheStorage.put(userProfileKey, userProfileCache);


        taskData.setContent(cacheData);
        finish(jobData, taskData);
    }


}
