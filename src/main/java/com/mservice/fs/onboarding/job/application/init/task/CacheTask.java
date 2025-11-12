package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.init.InitApplicationDataInfo;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class CacheTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "GET_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public CacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> onboardingData) throws BaseException, Exception {
        String serviceId = onboardingData.getServiceId();
        InitApplicationDataInfo initApplicationDataInfo = new InitApplicationDataInfo();

        String packageKey = PackageCache.createKey(serviceId, onboardingData.getInitiatorId());
        CacheData packageCache = cacheStorage.get(packageKey);
        Log.MAIN.info("Get package Cache with key: {} PackageCacheData: {}", packageKey, JsonUtil.toString(packageCache));
        initApplicationDataInfo.setCachePackage(packageCache);

        String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(serviceId, onboardingData.getInitiatorId());
        CacheData knockOutRuleCache = cacheStorage.get(KnockOutRuleResponse.createKeyCache(serviceId, onboardingData.getInitiatorId()));
        if (knockOutRuleCache != null) {
            KnockOutRuleResponse knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
            initApplicationDataInfo.setKnockOutRuleResponse(knockOutRuleResponse);
        }
        Log.MAIN.info("Get Knock out rule Cache with key: {} - KnockOutRuleCacheData {}", knockOutRuleKey, JsonUtil.toString(knockOutRuleCache));

        String pendingFormListCacheKey = ApplicationListWrapper.createKey(onboardingData.getServiceId(), onboardingData.getInitiatorId());
        CacheData pendingFormListCacheData = cacheStorage.get(pendingFormListCacheKey);
        if (Utils.isNotEmpty(pendingFormListCacheData)) {
            List<ApplicationForm> pendingForms = ((ApplicationListWrapper) pendingFormListCacheData.getObject()).getApplicationForms();
            Log.MAIN.info("Get pending form with key [{}]: {}", packageKey, JsonUtil.toString(pendingForms));
            initApplicationDataInfo.setPendingForms(pendingForms);
        }
        taskData.setContent(initApplicationDataInfo);
        finish(onboardingData, taskData);
    }

    @Override
    protected boolean isAsync() {
        return false;
    }
}
