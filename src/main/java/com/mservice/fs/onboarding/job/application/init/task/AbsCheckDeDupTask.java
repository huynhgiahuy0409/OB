package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.init.InitAdapterRequest;
import com.mservice.fs.onboarding.model.api.init.InitAdapterResponse;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Set;

/**
 * @author hoang.thai
 * on 11/17/2023
 */
public abstract class AbsCheckDeDupTask<T extends OnboardingRequest, R extends InitFormResponse> extends OnboardingSendAdapterTask<T, R, InitAdapterResponse> {

    public AbsCheckDeDupTask() {
        super();
    }

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public static final Set<ApplicationStatus> PARTNER_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    @Override
    protected boolean isAllowedSendAdapter(OnboardingData<T, R> onboardingData) throws BaseException, Exception, ValidatorException {
        return onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId()).isMatchAction(Action.CHECK_DE_DUP_PARTNER, onboardingData.getProcessName());
    }

    @Override
    protected String createAdapterRequest(OnboardingData<T, R> onboardingData) throws IOException {
        UserProfileInfo userProfileInfo = onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        T request = onboardingData.getRequest();
        InitAdapterRequest initAdapterRequest = JsonUtil.fromByteArray(onboardingData.getBase().getRequest(), InitAdapterRequest.class);
        initAdapterRequest.setRequestId(request.getRequestId());
        initAdapterRequest.setPartnerId(getPartnerId(onboardingData));
        initAdapterRequest.setPhoneNumber(onboardingData.getInitiator());
        initAdapterRequest.setPersonalId(userProfileInfo.getPersonalIdKyc());

        ApplicationForm applicationForm = getApplicationForm(onboardingData);
        ApplicationData applicationData = applicationForm.getApplicationData();
        initAdapterRequest.setApplicationData(applicationData);
        return initAdapterRequest.toString();
    }

    @Override
    protected void processAdapterResponse(TaskData taskData, OnboardingData<T, R> onboardingData, InitAdapterResponse initAdapterResponse) throws Exception, BaseException {
        String serviceId = onboardingData.getServiceId();
        String partnerId = getPartnerId(onboardingData);
        String agentId = onboardingData.getInitiatorId();

        ApplicationForm applicationForm = getApplicationForm(onboardingData);
        ApplicationData applicationData = applicationForm.getApplicationData();
        applicationData.setReasonId(initAdapterResponse.getReasonId());
        applicationData.setReasonMessage(initAdapterResponse.getReasonMessage());
        applicationData.setPartnerApplicationId(initAdapterResponse.getPartnerApplicationId());

        updateApplicationData(initAdapterResponse, applicationData);

        Log.MAIN.info("Check de dup success with serviceId {} - partnerId {} - agentId {} - response: {}", serviceId, partnerId, agentId, initAdapterResponse);
        finish(onboardingData, taskData);
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.PARTNER_SERVICE_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.PARTNER_SERVICE_ERROR;
    }

    @Override
    protected String getPartnerId(OnboardingData<T, R> onboardingData) {
        return Utils.coalesce(onboardingData.getPartnerId(), onboardingData.getRequest().getPartnerId());
    }

    @SneakyThrows
    @Override
    protected void fillUpDataToResponseWhenFailed(OnboardingData<T, R> jobData, InitAdapterResponse adapterResponse, R response, Integer platformResultCode) {
        ApplicationForm applicationForm = getApplicationForm(jobData);
        ApplicationData applicationData = applicationForm.getApplicationData();
        applicationData.setReasonId(adapterResponse.getReasonId());
        applicationData.setReasonMessage(adapterResponse.getReasonMessage());
        applicationData.setPartnerApplicationId(adapterResponse.getPartnerApplicationId());
        ApplicationStatus partnerStatus = adapterResponse.getPartnerStatus();
        if (partnerStatus == null) {
            Log.MAIN.info("Got partner status is null, do nothing !!! ");
            return;
        }
        if (!PARTNER_STATUS.contains(partnerStatus)) {
            Log.MAIN.error("Error when partner status adapter not in [{}]", PARTNER_STATUS);
            throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
        }
        Log.MAIN.info("Set app status to [{}]", partnerStatus);
        applicationData.setStatus(partnerStatus);
        applicationData.setState(partnerStatus.getState());
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.BANNED_BY_PARTNER, jobData.getProcessName()) && partnerStatus == ApplicationStatus.REJECTED_BY_LENDER) {
            Log.MAIN.info("Banned application by partner !!!");
            applicationData.setState(ApplicationState.BANNED);
        }
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        updateApplicationData(adapterResponse, applicationData);

        response.setApplicationData(applicationData);
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> jobData);

    @Override
    protected boolean isAsync() {
        return true;
    }

    private void updateApplicationData(InitAdapterResponse adapterResponse, ApplicationData applicationData) throws Exception {
        ApplicationData applicationDataAdapter = adapterResponse.getApplicationData();
        if (!Utils.isEmpty(applicationDataAdapter)) {
            OnboardingUtils.mappingApplicationData(applicationDataAdapter, applicationData);
        }
    }
}
