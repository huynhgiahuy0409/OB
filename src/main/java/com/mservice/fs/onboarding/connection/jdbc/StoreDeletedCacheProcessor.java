package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.crm.CrmCancelApplicationRequest;
import com.mservice.fs.onboarding.model.crm.CrmUpdateScamRequest;
import com.mservice.fs.onboarding.model.crm.LoanInfo;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author phat.duong1
 * on 25/11/2024
 */
public class StoreDeletedCacheProcessor extends CallableProcessor<Void> {

    public void execute(LoanInfo loanInfo, CrmCancelApplicationRequest request) throws BaseException, Exception {
        run(new CallableInputParam("p_application_id", loanInfo.getLoanId()),
                new CallableInputParam("p_service_id", loanInfo.getServiceId()),
                new CallableInputParam("p_phone_number", loanInfo.getPhoneNumber()),
                new CallableInputParam("p_form", loanInfo.toString()),
                new CallableInputParam("p_canceled_by", request.getCancelBy()),
                new CallableInputParam("p_reason", request.getReason()));
    }

    public void execute(ApplicationData applicationData, CrmUpdateScamRequest request) throws BaseException, Exception {
        run(new CallableInputParam("p_application_id", applicationData.getApplicationId()),
                new CallableInputParam("p_service_id", applicationData.getServiceId()),
                new CallableInputParam("p_phone_number", applicationData.getPhoneNumber()),
                new CallableInputParam("p_form", applicationData.toString()),
                new CallableInputParam("p_canceled_by", request.getScamStatus()),
                new CallableInputParam("p_reason", request.getScamStatus()));
    }

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws SQLException, BaseException {
        return null;
    }
}
