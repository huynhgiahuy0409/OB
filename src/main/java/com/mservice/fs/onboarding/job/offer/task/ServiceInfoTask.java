package com.mservice.fs.onboarding.job.offer.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.processor.TaskData;

/**
 * @author hoang.thai on 8/28/2023
 */
public class ServiceInfoTask extends OnboardingTask<OfferRequest, OfferResponse> {

    public static final TaskName NAME = () -> "GET_PACKAGE-AI";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ServiceInfoTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OfferRequest, OfferResponse> jobData) throws BaseException, Exception, ValidatorException {
        taskData.setContent(onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId()));
        finish(jobData, taskData);
    }
}
