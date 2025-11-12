package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ChosenPackage;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

public class ValidatePackageTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "VALIDATE_PACKAGE_TASK";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ValidatePackageTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        String serviceId = jobData.getServiceId();
        InitFormRequest request = jobData.getRequest();
        ChosenPackage chosenPackage = request.getChosenPackage();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        if (serviceObInfo.isMatchAction(Action.VALIDATE_PACKAGE, jobData.getProcessName())) {
            if (Utils.isEmpty(chosenPackage)) {
                Log.MAIN.info("Dose not have chosen package next task...");
                finish(jobData, taskData);
                return;
            }
            String partnerId = jobData.getPartnerId();
            PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(partnerId);
            //if partnerId not config => does not check
            Integer miniAppTrackVerConfig = partnerConfig.getMiniAppTrackVerPackage();
            if (Utils.isEmpty(miniAppTrackVerConfig)) {
                Log.MAIN.info("miniAppTrackVerPackage is empty with partnerId {} -> next task", partnerId);
                finish(jobData, taskData);
                return;
            }
            Log.MAIN.info("Start check package chosen package next task...");
            if (jobData.getRequest().getMetadata().getMiniAppTrackVer() < miniAppTrackVerConfig) {
                Log.MAIN.info("AgentId: {} - serviceId: {} - partnerId: {} - Package: {} - is not valid with miniAppTrackVer < miniAppTrackVerConfig ({} < {}) ", jobData.getInitiatorId(), serviceId, partnerId, Json.encode(chosenPackage), jobData.getRequest().getMetadata().getMiniAppTrackVer(), miniAppTrackVerConfig);
                InitFormResponse response = new InitFormResponse();
                response.setResultCode(OnboardingErrorCode.PACKAGE_IS_NOT_VALID_WITH_VERSION_APP);
                jobData.setResponse(response);
            }
        }
        finish(jobData, taskData);
    }
}
