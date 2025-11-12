package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import com.mservice.fs.onboarding.model.application.init.InitDataDB;

import java.sql.CallableStatement;
import java.sql.ResultSet;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class GetApiInitDataProcessor extends CallableProcessor<InitDataDB> {


    @Override
    protected InitDataDB processWithStatement(CallableStatement cs) throws Exception, BaseException {
        return InitDataDB.builder()
                .applicationByAgentId(JdbcTransformer.toObjects((ResultSet) cs.getObject("p_application_by_agent"), ApplicationDataInit.class))
                .build();
    }

    public InitDataDB execute(String agentId, String personalId, String serviceGroup) throws BaseException, Exception {
        try {
            return run(
                    new CallableInputParam("p_agent_id", agentId),
                    new CallableInputParam("p_personal_id", personalId),
                    new CallableInputParam("p_service_group", serviceGroup)
            );
        } catch (BaseException e) {
            Log.MAIN.error("[CreateApplicationIdProcessor] error when create application", e);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }

    }
}
