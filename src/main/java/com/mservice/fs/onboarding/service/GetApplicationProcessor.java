package com.mservice.fs.onboarding.service;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.utils.OnboardingDBUtils;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class GetApplicationProcessor extends CallableProcessor<ApplicationData> {

    public ApplicationData run(String applicationId, String phoneNumber) throws Exception, BaseException {
        return run( new CallableInputParam("p_application_id", applicationId),
                    new CallableInputParam("p_phone_number", getResource().getPhoneFormat().formatPhone10To11(phoneNumber))
                );
    }

    @Override
    protected ApplicationData processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ApplicationData applicationData = OnboardingDBUtils.loadApplicationData(cs);
        OnboardingDBUtils.loadAdditionalData(applicationData, cs);
        return applicationData;

    }
}
