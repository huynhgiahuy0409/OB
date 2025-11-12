package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.*;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.crm.*;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import java.util.ArrayList;
import java.util.List;

public class GetCacheTask<T extends CrmRequest, R extends CrmResponse> extends Task<PlatformData<T,R>,T,R, OnboardingConfig> {

    public static final TaskName NAME = () -> "GET_CACHE";

    @Autowire(name = "CacheStorage")
    protected RedisCacheStorage cacheStorage;


    public GetCacheTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<T, R> platformData) throws BaseException, Exception, ValidatorException {
        LoanListWrapper loanListWrapper = new LoanListWrapper();
        List<LoanInfo> loanInfos = new ArrayList<>();
        CrmConfig.Config config = platformData.getTaskData(LoadCrmConfigTask.NAME).getContent();
        UserProfileInfo userProfileInfo = platformData.getTaskData(CrmGetUserProfileTask.NAME).getContent();

        List<String> serviceIds = config.getServiceIds();
        String agentId = userProfileInfo.getAgent();
        List<ApplicationForm> applicationForms = new ArrayList<>();

        serviceIds.forEach(serviceId -> {
            String pendingFromKey = ApplicationListWrapper.createKey(serviceId, agentId);
            CacheData cacheData = cacheStorage.get(pendingFromKey);
            if(cacheData != null) {
                ApplicationListWrapper cacheObject = (ApplicationListWrapper) cacheData.getObject();
                applicationForms.addAll(cacheObject.getApplicationForms());
                updateCache(platformData, pendingFromKey, cacheData, loanListWrapper);
            }
        });

        applicationForms.forEach(form -> {
            LoanInfo loanInfo = new LoanInfo();
            ApplicationData data = form.getApplicationData();
            CrmConfig.Status status = config.getApplicationStatusMap().get(data.getStatus().name());

            loanInfo.setCreateTime(data.getCreatedDate());
            loanInfo.setPartnerId(data.getPartnerId());
            loanInfo.setLastModified(data.getModifiedDateInMillis());
            loanInfo.setPhoneNumber(data.getPhoneNumber());
            loanInfo.setFullName(data.getFullName());
            loanInfo.setServiceId(data.getServiceId());
            loanInfo.setPersonalId(data.getIdNumber());
            loanInfo.setStatus(data.getStatus());
            loanInfo.setLoanId(data.getApplicationId());
            loanInfo.setType(CrmType.CACHE);
            loanInfo.setCancel(status.isAllowDelete());
            loanInfo.setStatusMessage(status.getDescription());
            loanInfo.setScamStatus(data.getScamStatus());
            loanInfos.add(loanInfo);
        });
        loanListWrapper.setFetchList(loanInfos);
        doMoreAction(platformData, loanListWrapper);
        taskData.setContent(loanListWrapper);
        finish(platformData, taskData);
    }

    protected void doMoreAction(PlatformData<T,R> platformData, LoanListWrapper loanListWrapper) throws BaseException {
    }

    protected void updateCache(PlatformData<T,R> platformData, String key, CacheData cacheData, LoanListWrapper loanListWrapper) {
        //skip
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.TASK_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }
}
