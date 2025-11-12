package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.jdbc.GetApiInitDataProcessor;
import com.mservice.fs.onboarding.connection.jdbc.GetDeDupDataProcessor;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import com.mservice.fs.onboarding.model.application.init.InitDataDB;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.DateUtil;

import java.util.List;

/**
 * @author muoi.nong
 */
public class GetDBDeDupDataTask extends OnboardingTask<SubmitRequest, SubmitResponse> {

    public static final TaskName NAME = () -> "GET_DB_DATA";
    private static final String DATE_TYPE_KEY = "yyMMdd";

    @Autowire(name = "GetDeDupDataDB")
    private GetDeDupDataProcessor getDeDupDataProcessor;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public GetDBDeDupDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<SubmitRequest, SubmitResponse> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();

        List<ApplicationDataInit> dataDeDupList = getDeDupDataProcessor.execute(jobData.getInitiatorId(), userProfileInfo.getPersonalIdKyc(), serviceObInfo.getServiceGroup());
        taskData.setContent(dataDeDupList);

        finish(jobData, taskData);
    }
}
