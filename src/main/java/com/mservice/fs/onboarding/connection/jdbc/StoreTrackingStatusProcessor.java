package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.MappingRegistry;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.trackingstatus.TrackingModel;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author hoang.thai
 * on 11/8/2023
 */
public class StoreTrackingStatusProcessor<T extends OnboardingRequest, R extends OnboardingResponse> extends CallableProcessor<Void> {

    public void run(TrackingModel statusModel, OnboardingData<T, R> jobData) throws BaseException, Exception {
        run(MappingRegistry.convertToParams(statusModel));
    }

    @Override
    protected Void processWithStatement(CallableStatement callableStatement) throws SQLException, BaseException {
        return null;
    }
}
