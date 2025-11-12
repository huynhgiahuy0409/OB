package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.connection.jdbc.StoreApplicationDataProcessor;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.utils.constant.Constant;

import java.util.Set;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
public abstract class StoreRejectFormListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    private static final String NAME = "STORE_REJECT_FORM";
    private static final Set<Integer> ONBOARDING_REJECT_ERROR_CODES = Set.of(OnboardingErrorCode.KNOCK_OUT_RULE_BLOCK.getCode()
                                                                            , OnboardingErrorCode.KNOCK_OUT_RULE_REJECT.getCode()
                                                                            , OnboardingErrorCode.PACKAGE_AI_REJECT.getCode()
            );

    @Autowire(name = "InsertApplication")
    private StoreApplicationDataProcessor storeApplicationDataProcessor;

    public StoreRejectFormListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> onboardingData) throws Throwable {
        ApplicationForm applicationForm = getApplicationForm(onboardingData);
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (ONBOARDING_REJECT_ERROR_CODES.contains(resultCode) && applicationForm != null) {
            Log.MAIN.info("Start store pendingData resultCode {} in error in {}", resultCode, ONBOARDING_REJECT_ERROR_CODES);
            storeApplicationDataProcessor.store(applicationForm, onboardingData);
            return;
        }
        if (applicationForm != null && Constant.STORE_REJECTED_STATUS.contains(applicationForm.getApplicationData().getStatus())) {
            Log.MAIN.info("Start store status {} in reject status {}", applicationForm.getApplicationData().getStatus(), Constant.STORE_REJECTED_STATUS);
            storeApplicationDataProcessor.store(applicationForm, onboardingData);
        }
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> onboardingData);

}
