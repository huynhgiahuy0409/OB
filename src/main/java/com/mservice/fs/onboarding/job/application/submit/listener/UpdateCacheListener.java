package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.common.ai.UserActionEvent;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/13/2023
 */

public class UpdateCacheListener<T extends ConfirmRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "UPDATE_CACHE_APPLICATION";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public UpdateCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        CacheData cacheData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        TaskData applicationTask = jobData.getTaskData(ApplicationTask.NAME);
        if (cacheData != null && applicationTask != null && applicationTask.getContent() != null) {
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            ApplicationData applicationData = ((ApplicationForm) applicationTask.getContent()).getApplicationData();
            ApplicationStatus applicationStatus = applicationData.getStatus();

            Integer resultCode = jobData.getResponse().getResultCode();
            ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();

            String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            boolean isUpdateCacheTimeWhenBanned = false;
            if (serviceObInfo.isMatchAction(Action.UPDATE_CACHE_TIME_WHEN_BANNED, jobData.getProcessName())) {
                isUpdateCacheTimeWhenBanned = applicationData.getState().equals(ApplicationState.BANNED) && this.getConfig().getUpdateCacheTimeWhenBannedPartners().contains(jobData.getPartnerId());
                Log.MAIN.info("Match action [{}] - Do update isUpdateCacheTimeWhenBanned = [{}]", Action.UPDATE_CACHE_TIME_WHEN_BANNED, isUpdateCacheTimeWhenBanned);
            }
            if (isRemoveCache(applicationStatus, resultCode, serviceObInfo) && !isUpdateCacheTimeWhenBanned) {
                Log.MAIN.info("Remove cache for reject : [{}]", JsonUtil.toString(applicationForm));
                cacheStorage.remove(key);
            } else {
                updateApplicationForSaveCache(applicationForm, resultCode, serviceObInfo, jobData, cacheData, isUpdateCacheTimeWhenBanned);
                Log.MAIN.info("Save Pending form cache: key: {} - value: {}", key, JsonUtil.toString(cacheData.getObject()));
                if (serviceObInfo.isMatchAction(Action.REMIND_PENDING_FORM, jobData.getProcessName())) {
                    OnboardingUtils.updateCacheForNotify(jobData, serviceObInfo, applicationForm, cacheStorage);
                }
                OnboardingUtils.savePendingForm(cacheStorage, key, cacheData, OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo));
            }
        } else {
            Log.MAIN.info("Not update cache with cacheData {} - applicationDataTask {}", cacheData, applicationTask);
        }
    }

    private void updateApplicationForSaveCache(ApplicationForm applicationForm, Integer resultCode, ServiceObInfo serviceObInfo,
                                               OnboardingData<T, R> jobData, CacheData cacheData, boolean isUpdateCacheTimeWhenBanned) {
        if (CommonErrorCode.SUCCESS.getCode().equals(resultCode)) {
            Log.MAIN.info("Update for save Cache");
            applicationForm.setRedirectTo(serviceObInfo.getNextDirection(jobData.getProcessName()));
            applicationForm.getApplicationData().setModifiedDateInMillis(System.currentTimeMillis());
        }
        if (isUpdateCacheTimeWhenBanned) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            long expiredTimeInMillis = getExpiredTimeInMillis(jobData, serviceObInfo, applicationData);
            Log.MAIN.info("Set application data expired time to [{}]", expiredTimeInMillis);
            applicationData.setExpiredTimeInMillis(expiredTimeInMillis);
        }
        if (serviceObInfo.isMatchAction(Action.CLEAR_PENDING_FORM_BY_PARTNER, jobData.getProcessName())
                && CommonErrorCode.SUCCESS.getCode().equals(resultCode)
                && this.getConfig().getFinalSubmitClearPendingFormPartners().contains(jobData.getPartnerId())) {
            ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
            List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms().stream()
                    .filter(pendingForm -> !jobData.getPartnerId().equals(pendingForm.getApplicationData().getPartnerId()))
                    .toList();
            Log.MAIN.info("List application form after clear pending form by partner [{}]: {}", jobData.getPartnerId(), Json.encode(applicationForms));
            applicationListWrapper.setApplicationForms(applicationForms);
        }
        if (Utils.isNotEmpty(jobData.getRequest().getScamAlertResult())) {
            ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
            UserActionEvent userActionEvent = OnboardingUtils.buildUserActionEvent(jobData.getRequest().getScamAlertResult());
            Log.MAIN.info("UserActionEvent: {}", Json.encode(userActionEvent));
            applicationListWrapper.setUserActionEvent(userActionEvent);
        }
    }

    private long getExpiredTimeInMillis(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, ApplicationData applicationData) {
        GenerateOtpAdapterResponse adapterResponse = jobData.getTaskData(OnboardingSendAdapterTask.NAME).getContent();

        if (Utils.isNotEmpty(adapterResponse.getBannedFormCacheTimeInMillis()) && adapterResponse.getBannedFormCacheTimeInMillis() != 0) {
            Log.MAIN.info("BannedFormCacheTimeInMillis is [{}]", adapterResponse.getBannedFormCacheTimeInMillis());
            return applicationData.getCreatedDate() + adapterResponse.getBannedFormCacheTimeInMillis();
        }

        return applicationData.getCreatedDate() + serviceObInfo.getBannedFormCacheTimeInMillis();
    }

    private boolean isRemoveCache(ApplicationStatus applicationStatus, Integer resultCode, ServiceObInfo serviceObInfo) {
        return OnboardingErrorCode.LOAN_DECIDER_REJECT.getCode().equals(resultCode) ||
                Constant.PARTNER_REMOVED_CACHE_STATUS.contains(applicationStatus) ||
                (applicationStatus.getState() == ApplicationState.BANNED && serviceObInfo.isDeleteCacheWhenReject());
    }

}
