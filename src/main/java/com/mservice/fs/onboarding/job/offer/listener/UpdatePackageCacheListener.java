package com.mservice.fs.onboarding.job.offer.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.offer.task.CacheTask;
import com.mservice.fs.onboarding.job.offer.task.GetOfferKnockOutRuleTask;
import com.mservice.fs.onboarding.job.offer.task.GetPackageTask;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.offer.OfferData;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
public class UpdatePackageCacheListener extends OnboardingListener<OfferRequest, OfferResponse> {

    private static final String NAME = "UPDATE_PACKAGE_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public UpdatePackageCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<OfferRequest, OfferResponse> jobData) throws Throwable {
        OfferData offerData = jobData.getTaskData(CacheTask.NAME).getContent();
        PackageCache packageCache = jobData.getTaskData(GetPackageTask.NAME).getContent();
        if (offerData.getPackageCache() == null && packageCache != null) {
            CacheData cachePackageData = new CacheData(jobData.getTraceId(), PackageCache.TIME_SAVE_PACKAGE, jobData.getTaskData(GetPackageTask.NAME).getContent());
            String packageKey = PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save Package cache: key: {} - value: {}", packageKey, JsonUtil.toString(cachePackageData.getObject()));
            cacheStorage.put(packageKey, cachePackageData);
        }

        TaskData knockOutRuleTask = jobData.getTaskData(GetOfferKnockOutRuleTask.NAME);
        if (knockOutRuleTask != null && knockOutRuleTask.getContent() != null) {
            KnockOutRuleResponse knockOutRuleResponseFromTask = knockOutRuleTask.getContent();
            CacheData knockOutRuleCache = new CacheData();
            knockOutRuleCache.setCacheObject(knockOutRuleResponseFromTask);
            knockOutRuleCache.setExpiredTime(KnockOutRuleResponse.TIME_CACHE);
            String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save KnockOutRule cache: key: {} - value: {}", knockOutRuleKey, JsonUtil.toString(knockOutRuleCache));
            cacheStorage.put(knockOutRuleKey, knockOutRuleCache);
        }
    }
}
