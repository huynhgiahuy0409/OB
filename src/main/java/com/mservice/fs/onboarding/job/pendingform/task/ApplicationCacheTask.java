package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BackendFailureReason;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.LoanActionType;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.ScamStatus;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.pendingform.PendingData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
public class ApplicationCacheTask extends OnboardingTask<PendingFormRequest, PendingFormResponse> {

    public static final TaskName NAME = () -> "GET_APPLICATION_CACHE";
    public static final String APPLICATION_DATA_KEY_TEMPLATE = "applicationData";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ApplicationCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        PendingFormRequest request = jobData.getRequest();
        PendingData pendingData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        CacheData cacheData = pendingData.getPendingFormCache();
        if (Utils.isEmpty(cacheData)) {
            Log.MAIN.info("Cache Data is null with agentId: {}", jobData.getInitiatorId());
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }
        ApplicationListWrapper listCacheApplication = (ApplicationListWrapper) cacheData.getObject();
        List<ApplicationForm> applicationForms = listCacheApplication.getApplicationForms();
        ApplicationForm applicationForm = getApplicationCache(applicationForms, request.getApplicationId());
        jobData.getTemplateModel().put(APPLICATION_DATA_KEY_TEMPLATE, applicationForm.getApplicationData());

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (ScamStatus.PENDING.name().equals(applicationForm.getApplicationData().getScamStatus())) {
            LoanActionAiConfig loanActionAiConfig = serviceObInfo.getLoanDeciderConfigMap().get(LoanActionType.PENDING_APPLICATION.name());
            Log.MAIN.info("User is pending due to being flagged in suspected scam list");
            throw new BaseException(loanActionAiConfig.getResultCode(), BackendFailureReason.BUSINESS_RULE);

        }

        taskData.setContent(applicationForm);
        finish(jobData, taskData);
    }

    private ApplicationForm getApplicationCache(List<ApplicationForm> applicationCaches, String applicationId) throws BaseException {
        for (ApplicationForm applicationCache : applicationCaches) {
            if (applicationId.equals(applicationCache.getApplicationData().getApplicationId())) {
                return applicationCache;
            }
        }
        Log.MAIN.info("Can not get Application Data from cache with applicationID {}", applicationId);
        throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
    }
}
