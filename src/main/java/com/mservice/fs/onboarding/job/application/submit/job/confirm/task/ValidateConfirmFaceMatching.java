package com.mservice.fs.onboarding.job.application.submit.job.confirm.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingResponse;
import com.mservice.fs.onboarding.model.application.confirm.FaceData;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.template.TemplateMessage;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 1/9/2024
 */
public class ValidateConfirmFaceMatching extends OnboardingTask<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> {

    @Autowire(name = "ServiceConfigInfo")
    DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire
    private TemplateMessage templateMessage;
    private static final TaskName NAME = () -> "VALIDATE_FACE_MATCHING";

    public ValidateConfirmFaceMatching() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        ConfirmFaceMatchingRequest request = onboardingData.getRequest();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());

        ApplicationForm applicationForm = onboardingData.getTaskData(ApplicationTask.NAME).getContent();

        Integer resultCodeFace = serviceObInfo.getActionType().checkResultCodeFromActionFaceMatching(request.getKycResult());
        if (Utils.isNotEmpty(resultCodeFace)) {
            Log.MAIN.info("Check ResultCode From Action FaceMatching is not null {}", resultCodeFace);
            ConfirmFaceMatchingResponse response = new ConfirmFaceMatchingResponse();
            ApplicationData applicationData = applicationForm.getApplicationData();
            applicationData.setStatus(ApplicationStatus.REJECTED_BY_MOMO);
            applicationData.setReasonMessage(ReasonMessage.REJECTED_BY_LIMITED_FACE_MATCHING.getReason());
            applicationData.setReasonId(ReasonMessage.REJECTED_BY_LIMITED_FACE_MATCHING.getCode());
            response.setResultCode(resultCodeFace);
            onboardingData.setResponse(response);
            finish(onboardingData, taskData);
            return;
        }
        FaceData faceData = request.getKycResult().getFaceData();
        if (!OnboardingUtils.isFaceDataSuccess(faceData)) {
            Log.MAIN.info("Face data is null or result code faceData is not success !!");
            ConfirmFaceMatchingResponse response = new ConfirmFaceMatchingResponse();
            LoanDeciderData loanDeciderData = applicationForm.getApplicationData().getLoanDeciderData();

            if (serviceObInfo.isAiActionMappingLD()) {
                mappingLD(onboardingData, serviceObInfo, loanDeciderData, response);
                finish(onboardingData, taskData);
                return;
            }
            LoanActionType loanActionType = loanDeciderData.getRequiredActionLoanDecider();
            LoanActionAiConfig loanActionAiConfig = serviceObInfo.getLoanDeciderConfigMap().get(loanActionType.name());
            UserProfileInfo userProfileInfo = onboardingData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();
            response = OnboardingUtils.createAiRuleResponse(ConfirmFaceMatchingResponse.class, loanActionAiConfig, onboardingData, applicationForm);
            Integer resultCodeFaceMatching = serviceObInfo.getActionType().getResultCodeWithActionLoanDecider(loanActionAiConfig.getActionName(), userProfileInfo);
            if (Utils.isNotEmpty(resultCodeFaceMatching)) {
                response.setResultCode(resultCodeFaceMatching);
            }
            onboardingData.setResponse(response);
        }
        finish(onboardingData, taskData);
    }

    private void mappingLD(OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> onboardingData, ServiceObInfo serviceObInfo, LoanDeciderData loanDeciderData, ConfirmFaceMatchingResponse response) {
        UserProfileInfo userProfileInfo = onboardingData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();
        AiLoanActionConfig config = OnboardingUtils.mappingLoanDeciderAction(serviceObInfo, loanDeciderData.getRequiredActionLoanDeciderList(), userProfileInfo);
        response.setResultCode(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
        if (config != null) {
            onboardingData.putProcessNameToTemPlateModel(config.getRedirectProcessName());
            response.setResultCode(config.getResultCode());
        }
        onboardingData.setResponse(response);
    }
}
