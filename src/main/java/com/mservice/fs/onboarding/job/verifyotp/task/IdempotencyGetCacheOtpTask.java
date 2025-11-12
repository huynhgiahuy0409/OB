package com.mservice.fs.onboarding.job.verifyotp.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 1/15/2025
 **/
public class IdempotencyGetCacheOtpTask extends OtpGetCacheDataTask<VerifyOtpRequest, VerifyOtpResponse> {
    @Override
    protected void perform(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException, Exception, ValidatorException {
        String key = getKey(jobData);
        Log.MAIN.info("Idempotency key: [{}]", key);
        CacheData idempotencyCache = cacheStorage.get(getKey(jobData));

        if (Utils.isNotEmpty(idempotencyCache) && Utils.isNotEmpty(idempotencyCache.getObject())) {
            Log.MAIN.info("Have cache response idempotency");
            jobData.setResponse((VerifyOtpResponse) idempotencyCache.getObject());
            jobData.setLoadFromCache();
            finish(jobData, taskData);
            return;
        }

        super.perform(taskData, jobData);
    }

    protected String getKey(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        return onboardingData.getProcessName() + ":" + String.join("_", onboardingData.getServiceId(), onboardingData.getInitiatorId(), onboardingData.getRequest().getApplicationId());
    }
}
