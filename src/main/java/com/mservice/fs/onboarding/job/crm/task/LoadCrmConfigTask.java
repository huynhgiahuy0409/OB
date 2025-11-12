package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.crm.CrmConfig;
import com.mservice.fs.onboarding.model.crm.CrmRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;

public class LoadCrmConfigTask<T extends CrmRequest, R extends CrmResponse> extends Task<PlatformData<T,R>,T,R, OnboardingConfig> {

    public static final TaskName NAME = () -> "LOAD_CRM";

    @Autowire
    private DataService<CrmConfig> crmConfigDataService;

    public LoadCrmConfigTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<T, R> platformData) throws BaseException, Exception, ValidatorException {
        CrmConfig.Config config = crmConfigDataService.getData().getConfigMap().get(platformData.getServiceId());
        taskData.setContent(config);
        finish(platformData, taskData);
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
