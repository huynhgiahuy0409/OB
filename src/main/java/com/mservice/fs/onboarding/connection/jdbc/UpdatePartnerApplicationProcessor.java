package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;

import java.sql.CallableStatement;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
public class UpdatePartnerApplicationProcessor extends CallableProcessor<Void> {

    public Void execute(ApplicationData applicationData) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_APPLICATION_ID", applicationData.getApplicationId()),
                new CallableInputParam("P_SERVICE_ID", applicationData.getServiceId()),
                new CallableInputParam("P_AGENT_ID", applicationData.getAgentId()),
                new CallableInputParam("P_PARTNER_APPLICATION_ID", applicationData.getPartnerApplicationId())

        );
    }

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws Exception, BaseException {
        return null;
    }
}

