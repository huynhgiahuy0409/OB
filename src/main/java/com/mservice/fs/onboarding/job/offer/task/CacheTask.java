package com.mservice.fs.onboarding.job.offer.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.offer.OfferData;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
public class CacheTask extends OnboardingTask<OfferRequest, OfferResponse> {

    public static final TaskName NAME = () -> "GET_CACHE_PACKAGE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public CacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OfferRequest, OfferResponse> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();
        String agentId = jobData.getInitiatorId();

        OfferData offerData = new OfferData();

        CacheData cacheData = cacheStorage.get(PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId()));
        if (cacheData != null) {
            PackageCache packageCache = (PackageCache) cacheData.getObject();
            offerData.setPackageCache(packageCache);
        }

        CacheData knockOutRuleCache = cacheStorage.get(KnockOutRuleResponse.createKeyCache(serviceId, agentId));
        if (knockOutRuleCache != null) {
            KnockOutRuleResponse knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
            offerData.setKnockOutRuleResponse(knockOutRuleResponse);
        }

        taskData.setContent(offerData);
        finish(jobData, taskData);
    }
}
