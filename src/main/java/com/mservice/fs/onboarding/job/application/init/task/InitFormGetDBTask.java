package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.jdbc.GetApiInitDataProcessor;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.init.InitDataDB;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author muoi.nong
 */
public class InitFormGetDBTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "GET_DB_DATA";
    private static final String DATE_TYPE_KEY = "yyMMdd";

    @Autowire(name = "GetApiInitDataDB")
    private GetApiInitDataProcessor getApiInitDataProcessor;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public InitFormGetDBTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();

        InitDataDB initDataDB = getApiInitDataProcessor.execute(jobData.getInitiatorId(), userProfileInfo.getPersonalIdKyc(), serviceObInfo.getServiceGroup());
        taskData.setContent(initDataDB);

        finish(jobData, taskData);
    }

}
