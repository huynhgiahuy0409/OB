package com.mservice.fs.onboarding.job.application.step.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.step.task.GetCacheDataTask;
import com.mservice.fs.onboarding.job.application.step.task.UpdateApplicationDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.fillform.FillFormRequest;
import com.mservice.fs.onboarding.model.application.fillform.FillFormResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author hoang.thai
 * on 11/27/2023
 */
public class UpdateCacheListener extends OnboardingListener<FillFormRequest, FillFormResponse> {

    public static final String NAME = "SAVE_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public UpdateCacheListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<FillFormRequest, FillFormResponse> jobData) throws Throwable {
        if (jobData.getResponse().getResultCode().equals(CommonErrorCode.SUCCESS.getCode())) {
            ApplicationForm applicationForm = jobData.getTaskData(UpdateApplicationDataTask.NAME).getContent();
            ApplicationData applicationData = applicationForm.getApplicationData();
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            long timeCachePendingForm = OnboardingUtils.calculatePendingFormExpireTime(applicationData.getExpiredTimeInMillis(), serviceObInfo);

            CacheData cacheData = jobData.getTaskData(GetCacheDataTask.NAME).getContent();
            String key = ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId());
            Log.MAIN.info("Save Pending form cache: key: {} - value: {}", key, JsonUtil.toString(cacheData.getObject()));
            OnboardingUtils.savePendingForm(cacheStorage, key, cacheData, timeCachePendingForm);

            if (serviceObInfo.isMatchAction(Action.REMIND_PENDING_FORM, jobData.getProcessName())) {
                OnboardingUtils.updateCacheForNotify(jobData, serviceObInfo, applicationForm, cacheStorage);
            }
        }
    }

}
