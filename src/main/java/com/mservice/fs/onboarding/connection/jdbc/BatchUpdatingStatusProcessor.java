package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.UnsupportedDataTypeException;
import com.mservice.fs.jdbc.processor.BatchDbProcessor;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.onboarding.model.api.lock.InactiveRequest;

public class BatchUpdatingStatusProcessor extends BatchDbProcessor {

    public void execute(InactiveRequest inactiveRequest) throws UnsupportedDataTypeException, Exception {
        for (InactiveRequest.Application application : inactiveRequest.getApplications()) {
            addBatch(false,
                    new CallableInputParam("P_APPLICATION_ID", application.getApplicationId()),
                    new CallableInputParam("P_PHONE_NUMBER", application.getPhoneNumber()),
                    new CallableInputParam("P_LENDER_ID", application.getLenderId()),
                    new CallableInputParam("P_SERVICE_ID", application.getServiceId())
            );
        }
        executeImmediately();
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }

}
