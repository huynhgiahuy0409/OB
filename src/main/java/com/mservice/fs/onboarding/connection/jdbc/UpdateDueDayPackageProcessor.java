package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class UpdateDueDayPackageProcessor extends CallableProcessor<Void> {

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws SQLException, BaseException {
        return null;
    }

    public Void execute(String applicationId, String dueDay) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_APPLICATION_ID", applicationId),
                new CallableInputParam("P_DUE_DAY", dueDay)
        );
    }

}
