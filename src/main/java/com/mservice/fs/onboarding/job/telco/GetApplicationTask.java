package com.mservice.fs.onboarding.job.telco;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpRequest;
import com.mservice.fs.onboarding.model.OtpResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.processor.TaskData;

public class GetApplicationTask<T extends OtpRequest, R extends OtpResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_APPLICATION";

    public GetApplicationTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        CacheData pendingFormCache = jobData.getTaskData(GetCacheTask.NAME).getContent();

        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) pendingFormCache.getObject();

        ApplicationForm applicationForm = applicationListWrapper.getApplicationById(jobData.getRequest().getApplicationId());

        if (applicationForm == null) {
            Log.MAIN.error("Application data from cache not found !!!");
            throw new BaseException(OnboardingErrorCode.INVALID_APPLICATION_DATA);
        }
        ApplicationData applicationData = applicationForm.getApplicationData();
        Log.MAIN.info("Application data from cache [{}]", applicationData);

        taskData.setContent(applicationForm);

        finish(jobData, taskData);
    }
}

