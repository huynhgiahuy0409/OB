package com.mservice.fs.onboarding.job;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.utils.CommonConstant;

import java.util.Set;

public abstract class AbsPartnerResultAIListener<T extends OnboardingRequest, R extends OnboardingResponse> extends AbsAIListener<T, R> {

    private static final String NAME = "PUB_PARTNER_RESULT_MSG";
    private static final Set<ApplicationStatus> PARTNER_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    public AbsPartnerResultAIListener() {
        super(NAME);
    }

    @Override
    protected boolean isValid(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) {
        ApplicationForm applicationForm = getApplicationForm(onboardingData);
        if (applicationForm == null) {
            Log.MAIN.info("ApplicationForm is null, do not push event AI");
            return false;
        }
        ApplicationStatus status = applicationForm.getApplicationData().getStatus();
        if(PARTNER_STATUS.contains(status)) {
            Log.MAIN.info("Status in List, active sync AI");
            return true;
        }
        Log.MAIN.info("Status [{}] not in [{}], deactive to AI", status, PARTNER_STATUS);
        return false;
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) {
        return getApplicationForm(onboardingData).getApplicationData().getStatus().getMessageType();
    }

    @Override
    protected String getPartnerResponse(OnboardingData<T, R> onboardingData) {
        return CommonConstant.STRING_EMPTY;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<T, R> onboardingData) {
        return getApplicationForm(onboardingData).getApplicationData();
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> onboardingData);
}
