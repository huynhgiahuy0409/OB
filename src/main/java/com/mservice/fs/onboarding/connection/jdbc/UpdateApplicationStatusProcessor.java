package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class UpdateApplicationStatusProcessor<T extends OnboardingRequest, R extends OnboardingResponse> extends CallableProcessor<Void> {

    public void run(OnboardingData<T, R> jobData, String referenceId, ApplicationStatus status, String partnerApplicationId, int reasonId, String reasonMessage) throws BaseException, Exception {
        run(
                new CallableInputParam("P_SERVICE_ID", jobData.getServiceId()),
                new CallableInputParam("P_PARTNER_ID", jobData.getPartnerId()),
                new CallableInputParam("P_AGENT_ID", jobData.getInitiatorId()),
                new CallableInputParam("P_REFERENCE_ID", referenceId),
                new CallableInputParam("P_STATUS", status.name()),
                new CallableInputParam("P_PARTNER_APPLICATION_ID", partnerApplicationId),
                new CallableInputParam("P_STATE", status.getState().name()),
                new CallableInputParam("P_REASON_ID", reasonId),
                new CallableInputParam("P_REASON_MESSAGE", reasonMessage)
                );
    }

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws SQLException, BaseException {
        return null;
    }
}
