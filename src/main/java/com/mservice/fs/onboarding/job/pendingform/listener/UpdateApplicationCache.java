package com.mservice.fs.onboarding.job.pendingform.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.pendingform.task.ApplicationCacheTask;
import com.mservice.fs.onboarding.job.pendingform.task.GetCacheTask;
import com.mservice.fs.onboarding.job.pendingform.task.PendingFormKnockOutRule;
import com.mservice.fs.onboarding.job.pendingform.task.PendingFormPackageTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.pendingform.PendingData;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Set;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
public class UpdateApplicationCache extends OnboardingListener<PendingFormRequest, PendingFormResponse> {

    private static final String NAME = "UPDATE_APPLICATION_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final Set<Integer> UPDATED_RESULT_CODE = Set.of(
            OnboardingErrorCode.SUCCESS.getCode(),
            OnboardingErrorCode.KNOCK_OUT_RULE_REJECT.getCode(),
            OnboardingErrorCode.KNOCK_OUT_RULE_BLOCK.getCode(),
            OnboardingErrorCode.PACKAGE_AI_REJECT.getCode()
    );

    private static final Set<Integer> REMOVED_RESULT_CODE = Set.of(
            OnboardingErrorCode.KNOCK_OUT_RULE_REJECT.getCode(),
            OnboardingErrorCode.KNOCK_OUT_RULE_BLOCK.getCode()
    );

    private static final Set<Integer> RESULT_CODE_OUT_WHITELIST = Set.of(
            OnboardingErrorCode.PACKAGE_NOT_AVAILABLE.getCode(),
            OnboardingErrorCode.PACKAGE_NOT_EXIST.getCode()

    );

    public UpdateApplicationCache() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) throws Throwable {
        PendingFormResponse response = jobData.getResponse();
        PendingData pendingData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        Integer resultCode = response.getResultCode();
        CacheData cacheData = pendingData.getPendingFormCache();
        String applicationKey = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
        ApplicationForm applicationForm = getApplicationForm(jobData);

        if (Utils.isEmpty(applicationForm)) {
            Log.MAIN.info("ApplicationForm with applicationId {} - key {} is null -> return;", jobData.getRequest().getApplicationId(), applicationKey);
            return;
        }

        ApplicationData applicationData;
        ApplicationStatus status = null;
        ApplicationState state = null;
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        long timeSaveFormCache = serviceObInfo.getPendingFormCacheTimeInMillis();
        if (applicationForm != null) {
            applicationData = applicationForm.getApplicationData();
            timeSaveFormCache = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);
            status = applicationData.getStatus();
            state = applicationData.getState();
        }
        if (UPDATED_RESULT_CODE.contains(resultCode)
                || RESULT_CODE_OUT_WHITELIST.contains(resultCode)
                || Constant.PARTNER_REMOVED_CACHE_STATUS.contains(status)
                || (ApplicationState.BANNED.equals(state) && serviceObInfo.isDeleteCacheWhenReject())) {

            ApplicationListWrapper listCacheApplication = (ApplicationListWrapper) cacheData.getObject();
            List<ApplicationForm> applicationForms = listCacheApplication.getApplicationForms();
            if (REMOVED_RESULT_CODE.contains(resultCode)
                    || Constant.PARTNER_REMOVED_CACHE_STATUS.contains(status)
                    || (RESULT_CODE_OUT_WHITELIST.contains(resultCode) && serviceObInfo.isDeleteCacheOutWhiteList())
                    || (ApplicationState.BANNED.equals(state) && serviceObInfo.isDeleteCacheWhenReject())) {
                Log.MAIN.info("Clear application with resultCode {} -  application: {}", resultCode, applicationForm);
                applicationForms.remove(applicationForm);
            }
            OnboardingUtils.savePendingForm(cacheStorage, applicationKey, cacheData, timeSaveFormCache);
        }
        PackageCache packageCache = jobData.getTaskData(PendingFormPackageTask.NAME).getContent();
        if (pendingData.getPackageCache() == null && packageCache != null) {
            CacheData cachePackageData = new CacheData(jobData.getTraceId(), PackageCache.TIME_SAVE_PACKAGE, packageCache);
            String packageKey = PackageCache.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save Package cache: key: {} - value: {}", packageKey, JsonUtil.toString(cachePackageData.getObject()));
            cacheStorage.put(packageKey, cachePackageData);
        }

        KnockOutRuleResponse knockOutRuleResponseFromTask = jobData.getTaskData(PendingFormKnockOutRule.NAME).getContent();
        if (knockOutRuleResponseFromTask != null) {
            CacheData knockOutRuleCache = new CacheData();
            knockOutRuleCache.setCacheObject(knockOutRuleResponseFromTask);
            knockOutRuleCache.setExpiredTime(KnockOutRuleResponse.TIME_CACHE);
            String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save KnockOutRule cache: key: {} - value: {}", knockOutRuleKey, JsonUtil.toString(knockOutRuleCache));
            cacheStorage.put(knockOutRuleKey, knockOutRuleCache);
        }
    }

    private ApplicationForm getApplicationForm(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        TaskData applicationCacheTask = jobData.getTaskData(ApplicationCacheTask.NAME);
        if (applicationCacheTask == null) {
            return null;
        }
        return applicationCacheTask.getContent();
    }
}
