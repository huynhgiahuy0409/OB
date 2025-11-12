package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.UnsupportedDataTypeException;
import com.mservice.fs.jdbc.processor.BatchDbProcessor;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.onboarding.model.api.lock.ActiveRequest;

public class ActivateProcessor extends BatchDbProcessor {

    public void execute(ActiveRequest activeRequest) throws UnsupportedDataTypeException, Exception {
        for (ActiveRequest.Application application : activeRequest.getApplications()) {
            addBatch(false,
                    new CallableInputParam("P_APPLICATION_ID", application.getApplicationId()),
                    new CallableInputParam("P_PHONE_NUMBER", application.getPhoneNumber()),
                    new CallableInputParam("P_LENDER_ID", application.getLenderId()),
                    new CallableInputParam("P_SERVICE_ID", application.getServiceId()));
        }
        executeImmediately();
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }


}
