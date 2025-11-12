package com.mservice.fs.onboarding.job.notify.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserRequest;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserResponse;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.Utils;

import java.util.List;

public class ValidateTask extends Task<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "VALIDATE_TASK";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public ValidateTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<NotifyUserRequest, NotifyUserResponse> data) throws BaseException, Exception, ValidatorException {
        String serviceId = data.getServiceId();
        String key = ApplicationListWrapper.createKey(serviceId, data.getInitiatorId());
        CacheData cacheData = cacheStorage.get(key);
        Log.MAIN.info("Get Cache Application with key: {}", key);

        NotifyUserRequest request = data.getRequest();
        List<ApplicationForm> applicationForms = request.getApplicationForms();
        ApplicationData applicationData = applicationForms.getFirst().getApplicationData();
        String applicationId = applicationData.getApplicationId();
        ApplicationForm applicationForm = getPendingForm(cacheData, applicationId, data);
        if (applicationForm == null) {
            NotifyUserResponse response = new NotifyUserResponse();
            response.setResultCode(OnboardingErrorCode.CACHE_NOT_FOUND);
            data.setResponse(response);
        }
        taskData.setContent(applicationForm);
        finish(data, taskData);
    }

    private ApplicationForm getPendingForm(CacheData cacheData, String applicationId, PlatformData<NotifyUserRequest, NotifyUserResponse> platformData) {
        if (cacheData == null) {
            Log.MAIN.info("CacheData is empty");
            return null;
        }
        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
        if (Utils.isEmpty(applicationId)) {
            Log.MAIN.info("ApplicationId from Cache noti is empty => not send platform");
            return null;
        }
        if (Utils.isEmpty(applicationListWrapper)) {
            Log.MAIN.info("ApplicationListWrapper is empty, applicationId {} => not send platform", applicationId);
            return null;
        }

        List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms();
        if (Utils.isEmpty(applicationForms)) {
            Log.MAIN.info("ApplicationForms is empty, applicationId {} => not send platform", applicationId);
            return null;
        }
        for (ApplicationForm applicationForm : applicationForms) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            if (Utils.isEmpty(applicationData)) {
                Log.MAIN.info("ApplicationData is empty -> continue");
                continue;
            }
            if (applicationId.equals(applicationData.getApplicationId())) {
                Log.MAIN.info("Have ApplicationData {}", applicationData);
                platformData.getTaskData(SendPlatformTask.NAME).setContent(applicationForm);
                return applicationForm;
            }
        }
        return null;
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
