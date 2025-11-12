package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.MappingRegistry;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.db.UpdateRoutingResultDB;
import com.mservice.fs.onboarding.utils.constant.Constant;

import java.sql.CallableStatement;

public class UpdatingRoutingResultProcessor extends CallableProcessor<String> {

    public String execute(UpdateRoutingResultDB updateRoutingResultDB) throws BaseException, Exception {
        try {
            return run(MappingRegistry.convertToParams(updateRoutingResultDB));
        } catch (Exception | BaseException e) {
            Log.MAIN.error("[UpdatingRoutingResultProcessor] Error when execute get data from database: ", e);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }
    }

    @Override
    protected String processWithStatement(CallableStatement cs) throws Exception, BaseException {
        String phoneNumber = cs.getString("O_PHONE_NUMBER");
        Log.MAIN.info("PhoneNumber: {}", phoneNumber);
        return phoneNumber;
    }
}
