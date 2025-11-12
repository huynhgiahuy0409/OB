package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.SendApplicationDataPlatformListener;
import com.mservice.fs.onboarding.job.application.submit.CheckLoanDeciderTask;
import com.mservice.fs.onboarding.job.application.submit.SubmitSendAdapterTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.LoanActionType;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

public class SubmitFormSendApplicationDataPlatformListener extends SendApplicationDataPlatformListener<SubmitRequest, SubmitResponse> {
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected ApplicationData getApplicationData(OnboardingData<SubmitRequest, SubmitResponse> jobData) throws Exception {
        ApplicationData applicationData;
        try {
            applicationData = jobData.getResponse().getApplicationData();
        } catch (Exception e) {
            applicationData = null;
            // skip
        }
        if (Utils.isEmpty(applicationData)) {
            ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
            applicationData = Utils.isNotEmpty(applicationForm) ? applicationForm.getApplicationData() : null;
        }
        ApplicationData applicationDataClone = OnboardingUtils.copy(applicationData);
        GenerateOtpAdapterResponse adapterResponse = jobData.getTaskData(SubmitSendAdapterTask.NAME).getContent();
        if (Utils.isNotEmpty(adapterResponse) && Utils.isNotEmpty(applicationDataClone)) {
            applicationDataClone.setPartnerApplicationId(adapterResponse.getPartnerApplicationId());
            applicationDataClone.setReasonMessage(adapterResponse.getReasonMessage());
        }
        return applicationDataClone;
    }

    @Override
    protected TaskData getTaskDataSendAdapter(OnboardingData<SubmitRequest, SubmitResponse> jobData) {
        return jobData.getTaskData(SubmitSendAdapterTask.NAME);
    }

    @Override
    protected boolean isValidateAction(OnboardingData<SubmitRequest, SubmitResponse> jobData) throws BaseException, ValidatorException, Exception {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());

        TaskData loanDeciderTaskData = jobData.getTaskData(CheckLoanDeciderTask.NAME);
        if (Utils.isEmpty(loanDeciderTaskData) || Utils.isEmpty(loanDeciderTaskData.getContent())) {
            Log.MAIN.info("Loan Decider Data empty");
            return false;
        }

        ActionInfo actionInfo = serviceObInfo.getActionInfo(Action.SEND_PLATFORM_LOAN_ACTION, jobData.getProcessName());
        if (Utils.isEmpty(actionInfo)) {
            Log.MAIN.info("Action info is null");
            return false;
        }
        LoanDeciderResponse loanDeciderResponse = loanDeciderTaskData.getContent();
        if (Utils.isEmpty(loanDeciderResponse.getLoanDeciderRecord())) {
            Log.MAIN.info("Loan Decider Record empty");
            return false;
        }

        LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
        for (LoanAction aiLoanAction : loanDeciderRecord.getLoanAction()) {
            LoanActionType loanActionType = LoanActionType.valueOf(aiLoanAction.getActionName());
            if (actionInfo.getAllowFields().contains(loanActionType.name())) {
                Log.MAIN.info("Have loan action - {}", loanActionType.name());
                return true;
            }
        }

        Log.MAIN.info("Not have loan action need send platform");
        return false;
    }


    @Override
    protected String getProcessName(OnboardingData<SubmitRequest, SubmitResponse> baseData) {
        try {
            if (isValidateAction(baseData)) {
                return "send-loan-action";
            }
        } catch (BaseException | ValidatorException | Exception e) {
            // skip
        }
        return super.getProcessName(baseData);
    }
}
