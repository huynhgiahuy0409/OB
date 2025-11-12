package com.mservice.fs.onboarding.connection.jdbc.crm;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.crm.getapplication.CrmApplicationRequest;

import java.sql.CallableStatement;

/**
 * @author muoi.nong
 */
public class CancelApplicationInfoProcessor extends CallableProcessor<Void> {

    public Void execute(CrmApplicationRequest request, String agentId) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_APPLICATION_ID", request.getLoanId()),
                new CallableInputParam("P_SERVICE_ID", request.getServiceId()),
                new CallableInputParam("P_AGENT_ID", agentId),
                new CallableInputParam("P_STATUS", ApplicationStatus.CANCELED_BY_MOMO.name()),
                new CallableInputParam("P_STATE", ApplicationStatus.CANCELED_BY_MOMO.getState().name())
        );
    }

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws Exception, BaseException {
        return null;
    }
}

