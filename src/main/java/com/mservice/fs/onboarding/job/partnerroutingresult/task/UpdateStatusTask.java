package com.mservice.fs.onboarding.job.partnerroutingresult.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.UpdatingRoutingResultProcessor;
import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.RoutingPackageStatus;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.db.UpdateRoutingResultDB;
import com.mservice.fs.onboarding.model.partnerroutingresult.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingRequest;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
public class UpdateStatusTask extends OnboardingTask<PartnerRoutingRequest, PartnerRoutingResponse> {
    public static final TaskName NAME = () -> "UPDATE-DB";
    @Autowire
    UpdatingRoutingResultProcessor updatingRoutingResultProcessor;

    @Autowire(name = "ServiceConfigInfo")
    protected DataService<ServiceObConfig> onboardingDataInfo;

    public UpdateStatusTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        PartnerRoutingRequest request = onboardingData.getRequest();
        LoanDeciderRecord loanDeciderRecord = request.getLoanDeciderRecord();
        boolean result = false;
        if (Utils.isNotEmpty(loanDeciderRecord.getLoanAction())
                && AIStatus.APPROVE.equals(loanDeciderRecord.getLoanDeciderStatus())) {
            result = loanDeciderRecord.getLoanAction().stream().anyMatch(loanAction
                    -> "RE_APPLY".equals(loanAction.getActionName()));
        }
        Log.MAIN.info("Result is {}", result);
        ServiceObInfo serviceObInfo =
                onboardingDataInfo.getData().getServiceObInfoByLoanProductCode(loanDeciderRecord.getLoanProductCode().toLowerCase());

        if (!serviceObInfo.isReapplyWhenLenderReject()) {
            throw new BaseException(CommonErrorCode.INVALID_REQUEST);
        }
        onboardingData.getBase().setServiceId(serviceObInfo.getServiceId());
        onboardingData.getBase().setPartnerId(CommonConstant.STRING_EMPTY);
        UpdateRoutingResultDB routingResultDB = new UpdateRoutingResultDB()
                .setAgentId(loanDeciderRecord.getAgentId())
                .setPreviousRoutingStatus(RoutingPackageStatus.WAITING_AI.name())
                .setRoutingStatus(result ? RoutingPackageStatus.RE_APPLY.name() : RoutingPackageStatus.NO_PACKAGE.name())
                .setServiceId(serviceObInfo.getServiceId());

        String phoneNumber = updatingRoutingResultProcessor.execute(routingResultDB);
        onboardingData.getBase().setInitiator(phoneNumber);
        onboardingData.getBase().setInitiatorId(loanDeciderRecord.getAgentId());
        taskData.setContent(result);
        finish(onboardingData, taskData);
    }
}
