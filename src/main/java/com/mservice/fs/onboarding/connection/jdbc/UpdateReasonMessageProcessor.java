package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.utils.OnboardingDBUtils;
import com.mservice.fs.utils.Utils;

import java.sql.CallableStatement;

public class UpdateReasonMessageProcessor extends CallableProcessor<ApplicationData> {

    @Override
    protected ApplicationData processWithStatement(CallableStatement callableStatement) throws Exception, BaseException {
        Integer result = callableStatement.getInt("P_OUT");

        if(Utils.isEmpty(result) && result == 0){
            Log.MAIN.error("[UpdateReasonMessageProcessor] Update Reason Message failed");
            throw new BaseException(OnboardingErrorCode.UPDATE_REASON_MESSAGE_FAIL);
        }

        return OnboardingDBUtils.loadApplicationData(callableStatement);
    }

    public ApplicationData updateReasonMessage(String applicationId, String agentId, String reasonMessage, ApplicationStatus applicationStatus, Integer reasonId, String serviceId) throws BaseException, Exception {
        return run(
                new CallableInputParam("P_APPLICATION_ID", applicationId),
                new CallableInputParam("P_AGENT_ID", agentId),
                new CallableInputParam("P_SERVICE_ID", serviceId),
                new CallableInputParam("P_APPLICATION_STATUS", applicationStatus.name()),
                new CallableInputParam("P_REASON_ID", reasonId),
                new CallableInputParam("P_REASON_MESSAGE", reasonMessage));
    }


}
