package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author muoi.nong
 */
public class GetDeDupDataProcessor extends CallableProcessor<List<ApplicationDataInit>> {


    @Override
    protected List<ApplicationDataInit> processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ResultSet rsApplicationValidDeDup = (ResultSet) cs.getObject("p_application_by_agent");

        return JdbcTransformer.toObjects(rsApplicationValidDeDup, ApplicationDataInit.class);
    }

    public List<ApplicationDataInit> execute(String agentId, String personalId, String serviceGroup) throws BaseException, Exception {
        try {
            return run(
                    new CallableInputParam("p_agent_id", agentId),
                    new CallableInputParam("p_personal_id", personalId),
                    new CallableInputParam("p_service_group", serviceGroup)
            );
        } catch (BaseException e) {
            Log.MAIN.error("[GetDeDupDataProcessor] error when create application", e);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }

    }
}
