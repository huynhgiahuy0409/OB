package com.mservice.fs.onboarding.job.application.init.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import com.mservice.fs.onboarding.model.application.init.InitDataDB;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author muoi.nong
 */
public class InitFormCheckDeDupMomoTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "CHECK_DE_DUP_MOMO";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public InitFormCheckDeDupMomoTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        InitDataDB initDataDB = jobData.getTaskData(InitFormGetDBTask.NAME).getContent();

        if (serviceObInfo.isMatchAction(Action.CHECK_DE_DUP_ONBOARDING, jobData.getProcessName())) {
            Log.MAIN.info("Check de dup momo");
            checkDeDup(initDataDB.getApplicationByAgentId(), serviceObInfo);
        }

        finish(jobData, taskData);
    }

    private void checkDeDup(List<ApplicationDataInit> applicationDataInits, ServiceObInfo serviceObInfo) throws BaseException, JsonProcessingException {
        if (Utils.isEmpty(applicationDataInits)) {
            return;
        }

        for (ApplicationDataInit applicationDataInit : applicationDataInits) {
            if (serviceObInfo.getApplicationStatusHitDedup().contains(applicationDataInit.getStatus())) {
                Log.MAIN.info("Fail De dup Onboarding with application {}", JsonUtil.toString(applicationDataInit));
                throw new BaseException(OnboardingErrorCode.FAIL_CHECK_DE_DUP_MOMO);
            }
        }
    }
}
