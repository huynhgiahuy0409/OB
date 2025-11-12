package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.StoreApplicationDataProcessor;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.utils.constant.Constant;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class StoreApplicationDataListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "STORE_DB_APPLICATION";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Autowire(name = "InsertApplication")
    private StoreApplicationDataProcessor storeApplicationDataProcessor;

    public StoreApplicationDataListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        CacheData cacheData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        if (cacheData != null) {
            ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
            if (applicationForm == null || applicationForm.getApplicationData() == null) {
                Log.MAIN.info("Application Data is null - skip store db");
                return;
            }
            ApplicationData applicationData = applicationForm.getApplicationData();
            ApplicationStatus applicationStatus = applicationData.getStatus();
            Integer resultCode = jobData.getResponse().getResultCode();

            if (isStoreDB(resultCode, applicationStatus)) {
                applicationForm.getApplicationData().setModifiedDateInMillis(System.currentTimeMillis());
                Log.MAIN.info("Store Data");
                storeApplicationDataProcessor.store(applicationForm, jobData);
            }
        }
    }

    private boolean isStoreDB(Integer resultCode, ApplicationStatus applicationStatus) {
        return CommonErrorCode.SUCCESS.getCode().equals(resultCode) ||
                OnboardingErrorCode.LOAN_DECIDER_REJECT.getCode().equals(resultCode) ||
                Constant.STORE_REJECTED_STATUS.contains(applicationStatus);
    }

}
