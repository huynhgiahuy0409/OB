package com.mservice.fs.onboarding.service;

import com.mservice.fs.jdbc.mapping.MappingRegistry;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.db.UpdatingStatusDB;
import com.mservice.fs.onboarding.utils.OnboardingDBUtils;
import com.mservice.fs.onboarding.utils.OnboardingErrorCode;

import java.sql.CallableStatement;

public class UpdatingStatusProcessor extends CallableProcessor<ApplicationData> {

    public ApplicationData execute(UpdatingStatusDB updatingStatusDB) throws BaseException, Exception {
        return run(MappingRegistry.convertToParams(updatingStatusDB));
    }

    @Override
    protected ApplicationData processWithStatement(CallableStatement cs) throws Exception, BaseException {
        if (!isValidStatus(cs.getInt("P_VALID_STATUS"))) {
            throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
        }
        return OnboardingDBUtils.loadApplicationData(cs);
    }

    private boolean isValidStatus(int status) {
        return status == 1;
    }

}
