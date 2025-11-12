package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.PackageAiTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
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
 * @author hoang.thai on 8/28/2023
 */
public class PendingFormPackageTask extends PackageAiTask<PendingFormRequest, PendingFormResponse> {


    @Override
    protected void processWithBadRequest(GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<PendingFormRequest, PendingFormResponse> jobData, ServiceObInfo serviceObInfo) throws BaseException {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkGetOfferPackageWhenPackageNotFound(packageAiResponse, userProfileInfo, applicationForm, jobData);
        if (onboardingErrorCode == null) {
            //another service will return list null when 404 not found
            return;
        }
        if (onboardingErrorCode == OnboardingErrorCode.PACKAGE_AI_REJECT) {
            //whitelist for CLO
            Log.MAIN.info("Service do not checkGetOfferPackageWhenSuccess ");
            PendingFormResponse response = new PendingFormResponse();
            ApplicationData applicationData = applicationForm.getApplicationData();
            applicationData.setState(ApplicationState.BANNED);
            response.setRedirectTo(applicationForm.getRedirectTo());
            response.setApplicationData(applicationData);
            response.setResultCode(OnboardingErrorCode.PACKAGE_AI_REJECT);
            jobData.setResponse(response);
            return;
        }
        Log.MAIN.error("Can not handle with onboardingresultCode not in {}", onboardingErrorCode);
        throw new BaseException(OnboardingErrorCode.ONBOARDING_ERROR_CODE);
    }

    @Override
    protected String getSegmentUser(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        TaskData taskData = jobData.getTaskData(PendingFormKnockOutRule.NAME);
        if (Utils.isNotEmpty(taskData) && Utils.isNotEmpty(taskData.getContent())) {
            KnockOutRuleResponse packageResponse = jobData.getTaskData(PendingFormKnockOutRule.NAME).getContent();
            return packageResponse.getLoanDeciderRecord().getProfile().getSegmentUser();
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void processWhenSuccess(ServiceObInfo serviceObInfo, GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) {
        OnboardingErrorCode onboardingErrorCode = serviceObInfo.getActionType().checkGetOfferPackageWhenSuccess(packageAiResponse, userProfileInfo);
        if (onboardingErrorCode != null) {
            //CCM check re kyc in get package
            PendingFormResponse response = new PendingFormResponse();
            response.setResultCode(onboardingErrorCode);
            jobData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, jobData.getRequest().getApplicationId());
            jobData.putProcessNameToTemPlateModel(OnboardingProcessor.INIT_CONFIRM);
            jobData.setResponse(response);
        }
    }

}
