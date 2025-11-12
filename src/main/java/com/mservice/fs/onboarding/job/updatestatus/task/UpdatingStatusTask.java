package com.mservice.fs.onboarding.job.updatestatus.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.RoutingPackageStatus;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.db.UpdatingStatusDB;
import com.mservice.fs.onboarding.service.UpdatingStatusProcessor;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.Utils;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class UpdatingStatusTask<T extends UpdatingStatusRequest, R extends UpdatingStatusResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "UPDATE_STATUS";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    private Class<R> clazzResponse;

    public UpdatingStatusTask() {
        super(NAME);
        this.clazzResponse = Generics.getTypeParameter(getClass(), UpdatingStatusResponse.class);
    }

    @Autowire
    private UpdatingStatusProcessor updatingStatusProcessor;

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> onboardingData) throws BaseException, Exception, ValidatorException {
        UpdatingStatusRequest request = onboardingData.getRequest();
        ApplicationStatus status = request.getStatus();
        if (!allowedStatus(onboardingData)) {
            throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
        }
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        String strPreviousStatus = getStrPreviousStatus(onboardingData, status, serviceObInfo);
        UpdatingStatusDB updatingStatusDB = new UpdatingStatusDB();
        updatingStatusDB.setApplicationId(request.getApplicationId())
                .setPhoneNumber(getResource().getPhoneFormat().formatPhone10To11(request.getPhoneNumber()))
                .setStatus(status.name())
                .setState(status.getState().name())
                .setPreviousStatus(strPreviousStatus)
                .setServiceId(onboardingData.getServiceId())
                .setPartnerId(onboardingData.getPartnerId())
                .setRawRequest(Utils.cut(request.encode(), 1000))
                .setReasonId(request.getReasonId())
                .setReasonMessage(request.getReasonMessage());

        if (ApplicationStatus.REJECTED_BY_LENDER.equals(status) && serviceObInfo.isReapplyWhenLenderReject()) {
            updatingStatusDB.setRoutingPackageStatus(RoutingPackageStatus.WAITING_AI.name());
        }

        updateStatus(taskData, onboardingData, request, status, strPreviousStatus, updatingStatusDB);
        R response = Generics.createObject(clazzResponse);
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }

    protected abstract boolean allowedStatus(OnboardingData<T, R> onboardingData);

    protected String getStrPreviousStatus(OnboardingData<T, R> onboardingData, ApplicationStatus status, ServiceObInfo serviceObInfo) throws BaseException {
        Set<ApplicationStatus> previousStatus = status.getPreviousStatus();

        if (ApplicationStatus.CANCELED_BY_LENDER.equals(status) && Utils.isNotEmpty(serviceObInfo.getStatusAllowCancelByLender())) {
            return serviceObInfo.getStatusAllowCancelByLender().stream().map(ApplicationStatus::name).collect(Collectors.joining(CommonConstant.SPLITTER_COMMA));
        }

        if (ApplicationStatus.REJECTED_BY_LENDER.equals(status) && Utils.isNotEmpty(serviceObInfo.getStatusAllowRejectByLender())) {
            return serviceObInfo.getStatusAllowRejectByLender().stream().map(ApplicationStatus::name).collect(Collectors.joining(CommonConstant.SPLITTER_COMMA));
        }

        if (previousStatus == null) {
            Log.MAIN.error("Error when update status for request [{}], next status is null !!!", onboardingData.getRequest());
            throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
        }
        return previousStatus.stream().map(ApplicationStatus::name).collect(Collectors.joining(CommonConstant.SPLITTER_COMMA));
    }

    private void updateStatus(TaskData taskData, OnboardingData<T, R> onboardingData, UpdatingStatusRequest request, ApplicationStatus status, String previousStatus, UpdatingStatusDB updatingStatusDB) throws Exception, BaseException {
        try {
            ApplicationData applicationData = updatingStatusProcessor.execute(updatingStatusDB);
            onboardingData.getBase().setInitiatorId(applicationData.getAgentId());
            taskData.setContent(applicationData);
        } catch (BaseException ex) {
            Log.MAIN.error("Invalid update status for contract [{}] with status [{}], previous status not exist in db [{}] ", request.getApplicationId(), status.name(), previousStatus);
            throw ex;
        }
    }
}
