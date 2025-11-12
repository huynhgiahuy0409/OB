package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.PackageAiTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusKnockOutRuleTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.ReasonMessage;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class InitFormPackageAi extends PackageAiTask<InitFormRequest, InitFormResponse> {

    @Override
    protected void processWithBadRequest(GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<InitFormRequest, InitFormResponse> jobData, ServiceObInfo serviceObInfo) throws BaseException {
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkGetOfferPackageWhenPackageNotFound(packageAiResponse, userProfileInfo, applicationForm, jobData);
        if (onboardingErrorCode == null) {
            //another service will return list null when 404 not found
            return;
        }
        if (onboardingErrorCode == OnboardingErrorCode.PACKAGE_AI_REJECT) {
            //whitelist for CLO
            Log.MAIN.info("Service do not checkGetOfferPackageWhenSuccess ");
            InitFormResponse response = new InitFormResponse();
            ApplicationData applicationData = applicationForm.getApplicationData();
            applicationData.setStatus(ApplicationStatus.NO_OFFERS);
            applicationData.setReasonMessage(ReasonMessage.NOT_WHITELIST_BY_AI.getReason());
            applicationData.setState(ApplicationState.BANNED);
            response.setApplicationData(applicationData);
            response.setResultCode(OnboardingErrorCode.PACKAGE_AI_REJECT);
            jobData.setResponse(response);
            return;
        }
        Log.MAIN.error("Can not handle InitFormPackageAi.processWithBadRequest with onboardingresultCode {}", onboardingErrorCode);
        throw new BaseException(OnboardingErrorCode.ONBOARDING_ERROR_CODE);

    }

    @Override
    protected String getLenderId(InitFormRequest request) {
        return Utils.isEmpty(request.getLenderId()) ? super.getLenderId(request) : request.getLenderId();
    }

    @Override
    protected String getSegmentUser(OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        TaskData taskData = jobData.getTaskData(InitFormCheckKnockOutRuleTask.NAME);
        if (Utils.isNotEmpty(taskData) && Utils.isNotEmpty(taskData.getContent())) {
            KnockOutRuleResponse packageResponse = jobData.getTaskData(InitFormCheckKnockOutRuleTask.NAME).getContent();
            return packageResponse.getLoanDeciderRecord().getProfile().getSegmentUser();
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void processWhenSuccess(ServiceObInfo serviceObInfo, GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<InitFormRequest, InitFormResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(HandlePendingFormTask.NAME).getContent();
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkGetOfferPackageWhenSuccess(packageAiResponse, userProfileInfo);
        if (onboardingErrorCode != null) {
            //CCM check re kyc in get package
            InitFormResponse response = new InitFormResponse();
            response.setResultCode(onboardingErrorCode);
            jobData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, applicationForm.getApplicationData().getApplicationId());
            jobData.putProcessNameToTemPlateModel(OnboardingProcessor.INIT_CONFIRM);
            jobData.setResponse(response);
        }
    }
}
