package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationStatus;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
public class UpdateStatusProcessor extends CallableProcessor<Void> {

    @Override
    protected Void processWithStatement(CallableStatement cs) throws SQLException, BaseException {
        if (cs.getInt("P_COUNT") != 1) {
            throw new BaseException(OnboardingErrorCode.INVALID_APPLICATION_ID);
        }
        return null;
    }

    public Void execute(ApplicationStatus status, String applicationId) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_REFERENCE_ID", applicationId),
                new CallableInputParam("P_STATUS", status.name()),
                new CallableInputParam("P_STATE", status.getState().name())
        );
    }
}
