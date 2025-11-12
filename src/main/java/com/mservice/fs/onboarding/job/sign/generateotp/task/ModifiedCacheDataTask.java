package com.mservice.fs.onboarding.job.sign.generateotp.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.OtpGetCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpRequest;
import com.mservice.fs.onboarding.model.OtpResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Optional;

public class ModifiedCacheDataTask<T extends OtpRequest, R extends OtpResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "MODIFIED_CACHE_DATA";

    public ModifiedCacheDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {

        T request = jobData.getRequest();

        CacheData pendingFormCache = jobData.getTaskData(OtpGetCacheDataTask.NAME).getContent();

        if (Utils.isEmpty(pendingFormCache) || Utils.isEmpty(pendingFormCache.getObject())) {
            Log.MAIN.info("Cache pending form is empty!");
            finish(jobData, taskData);
            return;
        }

        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) pendingFormCache.getObject();
        List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms();

        Optional<ApplicationForm> applicationForm = applicationForms.stream()
                .filter(a -> a.getApplicationData().getApplicationId().equals(request.getApplicationId()))
                .findFirst();

        if (applicationForm.isPresent()) {
            ApplicationForm data = applicationForm.get();
            Log.MAIN.info("Application data from cache [{}]", data);

            taskData.setContent(data);
        }

        finish(jobData, taskData);
    }
}
