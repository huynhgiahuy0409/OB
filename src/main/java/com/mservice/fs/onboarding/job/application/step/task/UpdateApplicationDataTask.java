package com.mservice.fs.onboarding.job.application.step.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationInfo;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.fillform.FillFormRequest;
import com.mservice.fs.onboarding.model.application.fillform.FillFormResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public class UpdateApplicationDataTask extends OnboardingTask<FillFormRequest, FillFormResponse> {

    public static final TaskName NAME = () -> "UPDATE_PENDING_CACHE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataConfig;

    public UpdateApplicationDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<FillFormRequest, FillFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        FillFormRequest request = jobData.getRequest();
        CacheData cacheData = jobData.getTaskData(GetCacheDataTask.NAME).getContent();
        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
        List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms();
        ApplicationForm applicationForm = updateAndGetPendingForm(applicationForms, request, jobData, taskData);
        FillFormResponse response = new FillFormResponse();
        response.setApplicationData(applicationForm.getApplicationData());
        response.setResultCode(CommonErrorCode.SUCCESS);
        taskData.setContent(applicationForm);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    private ApplicationForm updateAndGetPendingForm(List<ApplicationForm> applicationCaches, FillFormRequest request, OnboardingData<FillFormRequest, FillFormResponse> jobData, TaskData taskData) throws Exception, BaseException, ValidatorException {
        for (ApplicationForm applicationForm : applicationCaches) {
            ApplicationData applicationData = applicationForm.getApplicationData();
            ApplicationInfo applicationInfo = request.getApplicationInfo();
            if (request.getApplicationId().equals(applicationData.getApplicationId())) {
                OnboardingUtils.mapApplicationInfoToApplicationData(applicationInfo, applicationData);
                String processName = jobData.getProcessName();
                ApplicationStatus status = processName.equals(OnboardingProcessor.FIRST_SUBMIT) ? ApplicationStatus.FIRST_SUBMIT : ApplicationStatus.SECOND_SUBMIT;
                applicationData.setStatus(status);
                applicationData.setState(status.getState());
                applicationForm.setRedirectTo(onboardingDataConfig.getData().getServiceObInfo(jobData.getServiceId()).getNextDirection(jobData.getProcessName()));
                applicationData.setModifiedDateInMillis(System.currentTimeMillis());
                return applicationForm;
            }
        }
        Log.MAIN.error("Pending Form does not exist with applicationId {} and application cache [{}]", request.getApplicationId(), Json.encode(applicationCaches));
        throw new BaseException(CommonErrorCode.SYSTEM_BUG);
    }


}
