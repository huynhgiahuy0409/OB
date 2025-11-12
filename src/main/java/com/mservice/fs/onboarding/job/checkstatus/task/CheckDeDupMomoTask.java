package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.ApplicationCheckStatusDbWrapper;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author muoi.nong
 */
public class CheckDeDupMomoTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "CHECK_DE_DUP_MOMO";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SERVICE_ID_DE_DUP = "serviceIdDeDup";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public CheckDeDupMomoTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        if (isActive(jobData, serviceObInfo)) {
            Log.MAIN.info("Check de dup momo");
            checkDeDup(jobData, getApplicationValidDedup(jobData), serviceObInfo);
        }

        finish(jobData, taskData);
    }

    private void checkDeDup(OnboardingData<T, R> jobData, ApplicationCheckStatusDbWrapper applicationDataInits, ServiceObInfo serviceObInfo) throws BaseException, Exception, ValidatorException {
        if (Utils.isEmpty(applicationDataInits)) {
            return;
        }
        Set<String> serviceIdSet = new HashSet<>();
        serviceIdSet.add(jobData.getServiceId());
        if (Utils.isNotEmpty(serviceObInfo.getServiceMerge())) {
            serviceIdSet.addAll(serviceObInfo.getServiceMerge());
        }

        List<ApplicationDataInit> applicationDbValidDeDups = applicationDataInits.getApplicationDbValidDeDups();
        for (ApplicationDataInit applicationDataInit : applicationDbValidDeDups) {
            if (serviceObInfo.getApplicationStatusHitDedup().contains(applicationDataInit.getStatus())) {
                Log.MAIN.info("Fail De dup Onboarding with application {}", JsonUtil.toString(applicationDataInit));
                if (serviceIdSet.contains(applicationDataInit.getServiceId())) {
                    throw new BaseException(OnboardingErrorCode.FAIL_CHECK_DE_DUP_MOMO);
                }
                ServiceObInfo serviceObInfoDeDup = onboardingDataInfo.getData().getServiceObInfo(applicationDataInit.getServiceId());
                jobData.getTemplateModel().put(SERVICE_NAME, serviceObInfoDeDup.getServiceName());
                jobData.getTemplateModel().put(SERVICE_ID_DE_DUP, applicationDataInit.getServiceId());
                throw new BaseException(OnboardingErrorCode.FAIL_CHECK_DE_DUP_DIFF_SERVICE);
            }
        }
    }

    protected ApplicationCheckStatusDbWrapper getApplicationValidDedup(OnboardingData<T, R> onboardingData) {
        return onboardingData.getTaskData(GetDataDeDupDBTask.NAME).getContent();
    }

    protected boolean isActive(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) {
        ApplicationCheckStatusDbWrapper dbWrapper = getApplicationValidDedup(onboardingData);

        List<ApplicationDataLite> dataLites = dbWrapper.getApplicationDataLites();
        for (ApplicationDataLite item : dataLites) {
            if (Utils.isNotEmpty(item.getStatus()) && serviceObInfo.getApplicationStatusHitDedup().contains(item.getStatus())) {
                return false;
            }
        }

        return serviceObInfo.isMatchAction(Action.CHECK_DE_DUP_ONBOARDING, onboardingData.getProcessName());
    }
}
