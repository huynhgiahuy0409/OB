package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hoang.thai on 10/30/2023
 */
public class PendingFormTask extends OnboardingTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final TaskName NAME = () -> "GET_PENDING_FORM_CACHE";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public PendingFormTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws BaseException, Exception {
        String serviceId = jobData.getServiceId();
        String agentId = jobData.getInitiatorId();
        CacheData cacheData = cacheStorage.get(ApplicationListWrapper.createKey(serviceId, agentId));
        List<ApplicationDataLite> applicationCaches = new ArrayList<>();
        if (cacheData != null) {
            ApplicationListWrapper cacheObject = (ApplicationListWrapper) cacheData.getObject();
            applicationCaches = Collections.singletonList(((ApplicationForm) cacheObject.getApplicationForms()).getApplicationData());
        }
        Log.MAIN.info("ServiceId {} - agentId {} - List pending form: {}", serviceId, agentId, applicationCaches);
        taskData.setContent(applicationCaches);
        finish(jobData, taskData);

    }

    @Override
    protected boolean isAsync() {
        return true;
    }
}
