package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.CheckLoanDeciderTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.config.MiniAppVersionDataService;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.util.List;

public class FinalSubmitLoanDeciderTask extends CheckLoanDeciderTask<SubmitRequest, SubmitResponse> {

    @Autowire
    private DataService<MiniAppVersionDataService> miniAppVersionDataService;

    @Override
    protected boolean isActionOtpTelco(OnboardingData<SubmitRequest, SubmitResponse> jobData, LoanDeciderResponse loanDeciderResponse) throws BaseException, ValidatorException, Exception {
        LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return false;
        }
        for (LoanAction action : loanActions) {
            if (TELCO_ACTION.equals(action.getActionName())) {
                Log.MAIN.info("IS ACTION OTP TELCO, DO CREATE RESPONSE !!!");
                OnboardingErrorCode errorCode = OnboardingErrorCode.ACTION_OTP_TELCO;
                SubmitResponse submitResponse = new SubmitResponse();
                if (miniAppVersionDataService.getData().isMiniAppVersionNotSupport(jobData, errorCode)) {
                    errorCode = OnboardingErrorCode.INVALID_MINI_APP_VERSION;
                }
                submitResponse.setResultCode(errorCode);
                jobData.setResponse(submitResponse);
                ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
                ApplicationData applicationData = applicationForm.getApplicationData();
                UserProfileInfo userProfileInfo = jobData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();

                applicationData.setTelco(1);
                applicationData.setCurrentCarrier(userProfileInfo.getCurrentCarrier());
                applicationData.setOriginalCarrier(userProfileInfo.getOriginalCarrier());
                return true;
            }
        }
        return false;
    }
}
