package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.template.TemplateMessage;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.Utils;

import java.util.List;

/**
 * @author hoang.thai
 * on 1/3/2024
 */
public class ExtraActions<T extends OnboardingRequest, R extends SubmitResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "VERIFY_FACE_MATCHING";

    @Autowire(name = "ServiceConfigInfo")
    DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire
    private TemplateMessage templateMessage;
    private final Class<?> responseClass;

    public ExtraActions() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(this.getClass(), OnboardingResponse.class);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (getConfig().getByPassBEForceFaceMatching().contains(jobData.getInitiatorId())) {
            Log.MAIN.info("Agent id {} in by pass Face Matching list: " + jobData.getInitiatorId());
        }
        if (serviceObInfo.isMatchAction(Action.VERIFY_FACE_MATCHING, jobData.getProcessName())
                && !getConfig().getByPassBEForceFaceMatching().contains(jobData.getInitiatorId())) {

            ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
            UserProfileInfo userProfileInfo = jobData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();

            if (!serviceObInfo.isAiActionMappingLD()) {
                processSingleActionDecider(jobData, serviceObInfo, applicationForm, userProfileInfo, jobData.getResponse());
            } else {
                processMappingActionDecider(jobData, applicationForm, serviceObInfo, userProfileInfo, jobData.getResponse());
            }
        }
        finish(jobData, taskData);
    }

    private void processMappingActionDecider(OnboardingData<T, R> jobData, ApplicationForm applicationForm, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, R response) throws ReflectiveOperationException {
        response = (R) Generics.createObject(responseClass);
        List<LoanActionType> loanActionTypeList = applicationForm.getApplicationData().getLoanDeciderData().getRequiredActionLoanDeciderList();

        if (loanActionTypeList.isEmpty()) {
            LoanActionType actionType = LoanActionType.FACE_MATCHING;
            loanActionTypeList.add(actionType);
        }

        AiLoanActionConfig config = OnboardingUtils.mappingLoanDeciderAction(serviceObInfo, loanActionTypeList, userProfileInfo);
        if (config == null) {
            response.setResultCode(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
            jobData.setResponse(response);
            return;
        }

        jobData.putProcessNameToTemPlateModel(config.getRedirectProcessName());
        response.setResultCode(config.getResultCode());
        jobData.setResponse(response);
    }

    private void processSingleActionDecider(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, UserProfileInfo userProfileInfo, R response) throws Exception {
        LoanActionType loanActionType = applicationForm.getApplicationData().getLoanDeciderData().getRequiredActionLoanDecider();
        if(Utils.isEmpty(loanActionType) && getConfig().isEnableBEForceFaceMatching()){
            loanActionType = LoanActionType.FACE_MATCHING;
            applicationForm.getApplicationData().getLoanDeciderData().setRequiredActionLoanDecider(loanActionType);
        }
        LoanActionAiConfig loanActionAiConfig = serviceObInfo.getLoanDeciderConfigMap().get(loanActionType.name());
        response = OnboardingUtils.createAiRuleResponse(responseClass, loanActionAiConfig, jobData, applicationForm);
        Integer resultCodeFaceMatching = serviceObInfo.getActionType().getResultCodeWithActionLoanDecider(loanActionAiConfig.getActionName(), userProfileInfo);
        if(Utils.isNotEmpty(resultCodeFaceMatching)){
            response.setResultCode(resultCodeFaceMatching);
        }
        jobData.setResponse(response);
    }
}
