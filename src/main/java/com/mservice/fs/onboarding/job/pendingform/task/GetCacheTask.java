package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.pendingform.PendingData;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class GetCacheTask extends OnboardingTask<PendingFormRequest, PendingFormResponse> {

    public static final TaskName NAME = () -> "GET_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public GetCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) throws BaseException, Exception {
        String serviceId = jobData.getServiceId();
        String agentId = jobData.getInitiatorId();

        PendingData pendingData = new PendingData();

        CacheData formData = cacheStorage.get(ApplicationListWrapper.createKey(serviceId, jobData.getInitiatorId()));
        if (formData != null) {
            Log.MAIN.info("ServiceId {} - agentId {} - cacheData {}", serviceId, agentId, JsonUtil.toString(formData));
            pendingData.setPendingFormCache(formData);
        }

        CacheData knockOutRuleCache = cacheStorage.get(KnockOutRuleResponse.createKeyCache(serviceId, agentId));
        if (knockOutRuleCache != null) {
            KnockOutRuleResponse knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
            pendingData.setKnockOutRuleResponse(knockOutRuleResponse);
        }

        CacheData packageData = cacheStorage.get(PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId()));
        if (packageData != null) {
            PackageCache packageCache = (PackageCache) packageData.getObject();
            pendingData.setPackageCache(packageCache);
        }

        taskData.setContent(pendingData);
        finish(jobData, taskData);
    }

}
