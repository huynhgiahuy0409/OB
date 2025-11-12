package com.mservice.fs.onboarding.job.application.submit;

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
import com.mservice.fs.utils.Generics;

/**
 * @author hoang.thai
 * on 11/22/2023
 */
public abstract class ModifyResponseTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    protected final Class<?> responseClass;

    public ModifyResponseTask() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(this.getClass(), OnboardingResponse.class);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        R response = createResponse(jobData);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    protected abstract R createResponse(OnboardingData<T, R> jobData) throws ReflectiveOperationException;

}
