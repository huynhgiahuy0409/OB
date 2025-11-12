package com.mservice.fs.onboarding.job.application.submit.job.confirm.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.CheckLoanDeciderTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmFaceMatchingResponse;
import com.mservice.fs.onboarding.model.application.confirm.FaceData;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.util.List;

public class ConfirmCheckLoanDeciderTask extends CheckLoanDeciderTask<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> {

    @Override
    protected boolean verifyFaceMatchingFromApp(OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> jobData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, UserProfileInfo userProfileInfo) {
        Log.MAIN.info("VerifyFaceMatchingFromApp with agentId {}", jobData.getInitiatorId());
        if (getConfig().isEnableVerifyFMConfirmAction()) {
            ConfirmFaceMatchingRequest request = jobData.getRequest();
            FaceData faceData = request.getKycResult().getFaceData();
            if (OnboardingUtils.isFaceDataSuccess(faceData)) {
                Log.MAIN.info("Face data success with agentId {}", jobData.getInitiatorId());
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isActionOtpTelco(OnboardingData<ConfirmFaceMatchingRequest, ConfirmFaceMatchingResponse> jobData, LoanDeciderResponse loanDeciderResponse) {
        LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return false;
        }
        for (LoanAction action : loanActions) {
            if (TELCO_ACTION.equals(action.getActionName())) {
                Log.MAIN.fatal("[CRITICAL] GOT ACTION OTP TELCO AT CONFIRM FACE MATCHING DO RETURN ERROR TO APP, AI NEED TO CHECK!!!");
                ConfirmFaceMatchingResponse submitResponse = new ConfirmFaceMatchingResponse();
                submitResponse.setResultCode(OnboardingErrorCode.INVALID_ACTION_OTP_TELCO);
                jobData.setResponse(submitResponse);
                return true;
            }
        }
        return false;
    }
}
