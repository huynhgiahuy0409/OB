package com.mservice.fs.onboarding.job.application.init.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.init.task.CacheTask;
import com.mservice.fs.onboarding.job.application.init.task.HandlePendingFormTask;
import com.mservice.fs.onboarding.job.application.init.task.InitFormCheckKnockOutRuleTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.init.InitApplicationDataInfo;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author hoang.thai
 * on 11/28/2023
 */
public class UpdateApplicationFormListener extends OnboardingListener<InitFormRequest, InitFormResponse> {

    private static final String NAME = "UPDATE_LISTENER_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    @Autowire(name = "CacheStorageRemind")
    private RedisCacheStorage cacheStorageRemind;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private Set<Integer> AI_REJECT_RESULT_CODE = Set.of(
            OnboardingErrorCode.KNOCK_OUT_RULE_BLOCK.getCode(),
            OnboardingErrorCode.KNOCK_OUT_RULE_REJECT.getCode()
    );

    public UpdateApplicationFormListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<InitFormRequest, InitFormResponse> onboardingData) throws Throwable {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        InitFormResponse response = onboardingData.getResponse();
        TaskData handlePendingFormTask = onboardingData.getTaskData(HandlePendingFormTask.NAME);
        if (handlePendingFormTask != null && handlePendingFormTask.getContent() != null) {

            ApplicationForm applicationForm = handlePendingFormTask.getContent();
            List<ApplicationForm> applicationForms = new ArrayList<>();
            InitApplicationDataInfo initApplicationDataInfo = onboardingData.getTaskData(CacheTask.NAME).getContent();
            if (serviceObInfo.isMatchAction(Action.CLEAR_PENDING_FORM_BY_PARTNER, onboardingData.getProcessName())
                    && Utils.isNotEmpty(initApplicationDataInfo) && Utils.isNotEmpty(initApplicationDataInfo.getPendingForms())) {
                Log.MAIN.info("Match action [{}] - Do NOT clear pending form of other partner", Action.CLEAR_PENDING_FORM_BY_PARTNER);
                initApplicationDataInfo.getPendingForms().stream()
                        .filter(pendingForm -> !onboardingData.getPartnerId().equals(pendingForm.getApplicationData().getPartnerId()))
                        .forEach(applicationForms::add);
            }
            applicationForms.add(applicationForm);
            ApplicationListWrapper applicationListWrapper = new ApplicationListWrapper();

            String key = ApplicationListWrapper.createKey(onboardingData.getServiceId(), onboardingData.getInitiatorId());
            ApplicationData applicationData = applicationForm.getApplicationData();
            ApplicationStatus status = applicationData.getStatus();
            if (isNotAddCache(status, applicationData, response.getResultCode(), serviceObInfo)) {
                Log.MAIN.info("Don't add ApplicationForm form cache with resultCode {} or status {} - applicationCache {}", response.getResultCode(), status, JsonUtil.toString(applicationForm));
            } else {
                Log.MAIN.info("Add ApplicationForm form cache with resultCode {} - applicationCache {}", response.getResultCode(), JsonUtil.toString(applicationForm));
                applicationListWrapper.setApplicationForms(applicationForms);
            }

            if (CommonErrorCode.SUCCESS.getCode().equals(response.getResultCode())) {
                String nextRedirect = serviceObInfo.getNextDirection(onboardingData.getProcessName());
                Log.MAIN.info("set Next redirect {}", nextRedirect);
                applicationForm.setRedirectTo(nextRedirect);

                if (serviceObInfo.isMatchAction(Action.REMIND_PENDING_FORM, onboardingData.getProcessName())
                        && Utils.isNotEmpty(serviceObInfo.getTimeRemindUser())) {
                    ApplicationListWrapper applicationNotifyWrapper = new ApplicationListWrapper();
                    applicationNotifyWrapper.buildDataForNotify(applicationData.getApplicationId());
                    OnboardingUtils.addCacheForNotify(onboardingData, serviceObInfo.getTimeRemindUser().longValue(), applicationNotifyWrapper, cacheStorageRemind);
                }
            }

            CacheData pendingFormCache = new CacheData();
            pendingFormCache.setCacheObject(applicationListWrapper);
            Log.MAIN.info("Save Pending form cache: key: {} - value: {}", key, JsonUtil.toString(pendingFormCache.getObject()));
            OnboardingUtils.savePendingForm(cacheStorage, key, pendingFormCache, OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo));
        }

        KnockOutRuleResponse knockOutRuleResponseFromTask = onboardingData.getTaskData(InitFormCheckKnockOutRuleTask.NAME).getContent();
        if (knockOutRuleResponseFromTask != null) {
            CacheData knockOutRuleCache = new CacheData();
            knockOutRuleCache.setCacheObject(knockOutRuleResponseFromTask);
            knockOutRuleCache.setExpiredTime(KnockOutRuleResponse.TIME_CACHE);
            String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(onboardingData.getServiceId(), onboardingData.getInitiatorId());
            Log.MAIN.info("Save KnockOutRule cache: key: {} - value: {}", knockOutRuleKey, JsonUtil.toString(knockOutRuleCache));
            cacheStorage.put(knockOutRuleKey, knockOutRuleCache);
        }

    }

    private boolean isNotAddCache(ApplicationStatus status, ApplicationData applicationData, Integer resultCode, ServiceObInfo serviceObInfo) {
        if (ApplicationState.BANNED.equals(applicationData.getState()) && serviceObInfo.isDeleteCacheWhenReject()) {
            updateCacheTimeWhenBanned(applicationData, serviceObInfo);
            return false;
        }
        return AI_REJECT_RESULT_CODE.contains(resultCode) ||
                Constant.PARTNER_REMOVED_CACHE_STATUS.contains(status);
    }

    private void updateCacheTimeWhenBanned(ApplicationData applicationData, ServiceObInfo serviceObInfo) {
        applicationData.setExpiredTimeInMillis(System.currentTimeMillis() + serviceObInfo.getBannedFormCacheTimeInMillis());
    }
}
