package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.application.submit.GetCacheTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.Image;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class ApplicationTask<T extends ConfirmRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "STORE_APPLICATION";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ApplicationTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        T request = jobData.getRequest();
        UserProfileInfo userProfileInfo = jobData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        CacheData cacheData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
        List<ApplicationForm> applicationForms = applicationListWrapper.getApplicationForms();
        ApplicationForm applicationForm = getApplicationForm(applicationForms, request.getApplicationId());
        updateApplicationForm(applicationForm, request, jobData, userProfileInfo, serviceObInfo);
        updateApplicationStatus(applicationForm, jobData, userProfileInfo);
        jobData.getTemplateModel().put(Constant.APPLICATION_DATA, applicationForm.getApplicationData());
        taskData.setContent(applicationForm);
        finish(jobData, taskData);
    }

    protected void updateApplicationForm(ApplicationForm applicationForm, T request, OnboardingData<T, R> jobData, UserProfileInfo userProfileInfo, ServiceObInfo serviceObInfo) throws Exception {

    }

    void updateApplicationStatus(ApplicationForm applicationForm, OnboardingData<T, R> jobData, UserProfileInfo userProfileInfo) {
        Log.MAIN.info("Start update application data with applicationId {} - phoneNumber {}", applicationForm.getApplicationData().getApplicationId(), applicationForm.getApplicationData().getInitiator());

        ApplicationData applicationData = applicationForm.getApplicationData();
        OnboardingUtils.setImageApplicationFromUserProfile(applicationData, userProfileInfo);

    }

    private ApplicationForm getApplicationForm(List<ApplicationForm> applicationForms, String applicationId) throws BaseException {
        for (ApplicationForm applicationForm : applicationForms) {
            if (applicationId.equals(applicationForm.getApplicationData().getApplicationId())) {
                return applicationForm;
            }
        }
        Log.MAIN.info("Can not get Application Data from cache with applicationID {}", applicationId);
        throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
    }

}
