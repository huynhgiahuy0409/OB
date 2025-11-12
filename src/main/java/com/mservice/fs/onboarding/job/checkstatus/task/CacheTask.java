package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hoang.thai on 10/30/2023
 */
public class CacheTask extends OnboardingTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final TaskName NAME = () -> "GET_PENDING_FORM_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public CacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws BaseException, Exception {
        String serviceId = jobData.getServiceId();
        String agentId = jobData.getInitiatorId();

        String pendingFromKey = ApplicationListWrapper.createKey(serviceId, agentId);
        String knockoutRuleKey = KnockOutRuleResponse.createKeyCache(serviceId, agentId);
        String packageKey = PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId());

        CacheData pendingFormCache = cacheStorage.get(pendingFromKey);
        Log.MAIN.info("Get Pending cache: key: {} - value: {}", pendingFromKey, JsonUtil.toString(pendingFormCache));
        List<ApplicationForm> applicationForms = new ArrayList<>();
        if (pendingFormCache != null) {
            ApplicationListWrapper cacheObject = (ApplicationListWrapper) pendingFormCache.getObject();
            applicationForms = cacheObject.getApplicationForms();
        }
        Log.MAIN.info("ServiceId {} - agentId {} - List pending form: {}", serviceId, agentId, JsonUtil.toString(applicationForms));
        CheckStatusData checkStatusData = new CheckStatusData();
        checkStatusData.setApplicationForms(applicationForms);

        CacheData knockOutRuleCache = cacheStorage.get(knockoutRuleKey);
        if (knockOutRuleCache != null) {
            KnockOutRuleResponse knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
            checkStatusData.setKnockOutRuleResponse(knockOutRuleResponse);
        }

        CacheData cacheData = cacheStorage.get(packageKey);
        if (cacheData != null) {
            PackageCache packageCache = (PackageCache) cacheData.getObject();
            checkStatusData.setPackageCache(packageCache);
        }
        taskData.setContent(checkStatusData);
        finish(jobData, taskData);

    }


    @Override
    protected boolean isAsync() {
        return true;
    }
}
