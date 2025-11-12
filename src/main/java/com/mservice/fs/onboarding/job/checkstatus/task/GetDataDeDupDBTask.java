package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.jdbc.ListApplicationProcessor;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.ApplicationCheckStatusDbWrapper;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 11/6/2023
 */
public class GetDataDeDupDBTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_SUBMITTED_FORM-DB";
    @Autowire(name = "GetAllApplicationForms")
    private ListApplicationProcessor listApplicationProcessor;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public GetDataDeDupDBTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {

        String serviceId = jobData.getServiceId();
        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);

        if (Utils.isNotEmpty(serviceObInfo.getServiceMerge())) {
            serviceId = String.join("|", serviceId, String.join("|", serviceObInfo.getServiceMerge()));
        }

        ApplicationCheckStatusDbWrapper checkStatusDbWrapper = listApplicationProcessor.execute(serviceId, jobData.getInitiatorId(), userProfileInfo.getPersonalIdKyc(), serviceObInfo.getServiceGroup());
        Log.MAIN.info("Submitted forms: {}", JsonUtil.toString(checkStatusDbWrapper.getApplicationDataLites()));
        taskData.setContent(checkStatusDbWrapper);
        finish(jobData, taskData);
    }
}
