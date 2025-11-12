package com.mservice.fs.onboarding.service;

import com.mservice.fs.generic.service.DataCreator;
import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.common.config.MiniAppVersionDataService;

import java.sql.CallableStatement;
import java.sql.ResultSet;

public class MiniAppConfigProcessor extends CallableProcessor<MiniAppVersionDataService> implements DataCreator<MiniAppVersionDataService> {

    @Override
    public MiniAppVersionDataService create() throws Exception, BaseException {
        return run();
    }

    @Override
    protected MiniAppVersionDataService processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ResultSet rs = (ResultSet) cs.getObject("P_OUT");
        MiniAppVersionDataService miniAppVersionDataService = new MiniAppVersionDataService();
        JdbcTransformer.toObjects(rs, MiniAppVersionDataService.MiniAppVersionConfig.class, miniAppVersionDataService::addAppVersion);
        return miniAppVersionDataService;
    }
}
