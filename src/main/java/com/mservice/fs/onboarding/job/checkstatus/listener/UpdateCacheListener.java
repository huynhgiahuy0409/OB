package com.mservice.fs.onboarding.job.checkstatus.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.checkstatus.task.*;
import com.mservice.fs.onboarding.job.checkstatus.task.CacheTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusKnockOutRuleTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusPackageTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ModifyResponseTask;
import com.mservice.fs.onboarding.job.checkstatus.task.SegmentUserTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.model.status.SegmentData;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/27/2023
 */
public class UpdateCacheListener extends OnboardingListener<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final String NAME = "SAVE_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public UpdateCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws Throwable {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();

        if (checkStatusData == null) {
            Log.MAIN.info("CacheData is null - skip update cache");
            return;
        }
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        PackageCache packageCache = jobData.getTaskData(CheckStatusPackageTask.NAME).getContent();
        if ((checkStatusData.getPackageCache() == null || serviceObInfo.isRecheckPendingForm()) && isGetPackage(jobData, serviceObInfo) && packageCache != null) {
            CacheData cachePackageData = new CacheData(jobData.getTraceId(), PackageCache.TIME_SAVE_PACKAGE, packageCache);
            String packageKey = PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save Package cache: key: {} - value: {}", packageKey, JsonUtil.toString(cachePackageData.getObject()));
            cacheStorage.put(packageKey, cachePackageData);
        }

        List<ApplicationForm> applicationCaches = checkStatusData.getApplicationForms();
        TaskData recheckPendingFormData = jobData.getTaskData(ModifyResponseTask.NAME);
        if ((serviceObInfo.isRecheckPendingForm() && recheckPendingFormData != null && recheckPendingFormData.getContent() != null)
                || (checkStatusData.getModifyApplicationForm() != null)) {
            String pendingFromKey = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            ApplicationListWrapper formCacheObject = new ApplicationListWrapper();
            if (Utils.isEmpty(applicationCaches)) {
                Log.MAIN.info("Remove key:{}", pendingFromKey);
                cacheStorage.remove(pendingFromKey);
            } else {
                ApplicationForm lastForm = applicationCaches.getLast();
                ApplicationData applicationData = lastForm.getApplicationData();
                formCacheObject.setApplicationForms(applicationCaches);
                long expiredTimeSaveCache = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);
                CacheData pendingFormDataCache = new CacheData(jobData.getTraceId(), expiredTimeSaveCache, formCacheObject);
                Log.MAIN.info("Save Pending Form cache: key: {} - value: {}", pendingFromKey, JsonUtil.toString(formCacheObject));
                OnboardingUtils.savePendingForm(cacheStorage, pendingFromKey, pendingFormDataCache, expiredTimeSaveCache);
            }
        }

        TaskData knockOutRuleTask = jobData.getTaskData(CheckStatusKnockOutRuleTask.NAME);
        if (knockOutRuleTask != null && knockOutRuleTask.getContent() != null) {
            KnockOutRuleResponse knockOutRuleResponseFromTask = jobData.getTaskData(CheckStatusKnockOutRuleTask.NAME).getContent();
            CacheData knockOutRuleCache = new CacheData();
            knockOutRuleCache.setCacheObject(knockOutRuleResponseFromTask);
            knockOutRuleCache.setExpiredTime(KnockOutRuleResponse.TIME_CACHE);
            String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save KnockOutRule cache: key: {} - value: {}", knockOutRuleKey, JsonUtil.toString(knockOutRuleCache));
            cacheStorage.put(knockOutRuleKey, knockOutRuleCache);
        }
    }

    private boolean isGetPackage(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, ServiceObInfo serviceObInfo) {
        TaskData segmentTaskData = jobData.getTaskData(SegmentUserTask.NAME);
        if (Utils.isEmpty(segmentTaskData) || Utils.isEmpty(segmentTaskData.getContent())) {
            return false;
        }
        SegmentData segmentData = segmentTaskData.getContent();
        UserType userType = segmentData.getUserType();
        if (!serviceObInfo.getUserTypeGetPackage().contains(userType)) {
            Log.MAIN.info("ServiceId: {} - agentId - {} userType {} - dose not need to call getPackage, by pass..", jobData.getServiceId(), jobData.getInitiatorId(), userType);
            return false;
        }
        return true;
    }

}
