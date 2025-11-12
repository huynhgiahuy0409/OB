package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.processor.TaskData;

/**
 * @author hoang.thai
 * on 11/30/2023
 */
public class ServiceObInfoTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_SERVICE_INFO_TASK";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ServiceObInfoTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        taskData.setContent(onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId()));
        finish(jobData, taskData);
    }


}
