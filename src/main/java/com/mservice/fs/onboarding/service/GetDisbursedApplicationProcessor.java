package com.mservice.fs.onboarding.service;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.disburse.ApplicationDisbursedData;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.List;

public class GetDisbursedApplicationProcessor extends CallableProcessor<List<ApplicationDisbursedData>> {

    public List<ApplicationDisbursedData> execute(String phoneNumber) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_PHONE_NUMBER", getResource().getPhoneFormat().formatPhone11To10(phoneNumber))
        );
    }

    @Override
    protected List<ApplicationDisbursedData> processWithStatement(CallableStatement cs) throws Exception, BaseException {
        return JdbcTransformer.toObjects((ResultSet) cs.getObject("P_OUT"), ApplicationDisbursedData.class);
    }
}
