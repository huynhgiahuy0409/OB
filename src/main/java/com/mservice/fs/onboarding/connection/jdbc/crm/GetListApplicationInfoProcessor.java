package com.mservice.fs.onboarding.connection.jdbc.crm;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.crm.getapplication.CrmApplicationInfo;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author muoi.nong
 */
public class GetListApplicationInfoProcessor extends CallableProcessor<List<CrmApplicationInfo>> {

    @Override
    protected List<CrmApplicationInfo> processWithStatement(CallableStatement cs) throws Exception, BaseException {
        return JdbcTransformer.toObjects((ResultSet) cs.getObject("P_RESULT"), CrmApplicationInfo.class);
    }

    public List<CrmApplicationInfo> execute(String serviceId, String agentId, String applicationId) throws BaseException, Exception {
        try {
            return run(
                    new CallableInputParam("P_SERVICE_ID", serviceId),
                    new CallableInputParam("P_AGENT_ID", agentId),
                    new CallableInputParam("P_APPLICATION_ID", applicationId)
            );
        } catch (BaseException e) {
            Log.MAIN.error("[GetListApplicationInfoProcessor] error when create application", e);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }

    }
}
