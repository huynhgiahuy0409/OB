package com.mservice.fs.onboarding.service;

import com.mservice.fs.generic.service.DataCreator;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.crm.CrmConfig;
import com.mservice.fs.onboarding.utils.constant.Constant;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CrmServiceProcessor extends CallableProcessor<CrmConfig> implements DataCreator<CrmConfig> {

    @Override
    public CrmConfig create() throws Exception, BaseException {
        return run();
    }

    @Override
    protected CrmConfig processWithStatement(CallableStatement cs) throws Exception, BaseException {
        Map<String, Set<String>> productServiceMap = getProductServiceMap(cs);
        CrmConfig crmConfig = getCrmConfig(cs, productServiceMap);
        ResultSet rsStatusDes = (ResultSet) cs.getObject("P_CRM_STATUS_DES");
        while (rsStatusDes.next()) {
            CrmConfig.Status status = new CrmConfig.Status();
            status.setProductName(rsStatusDes.getString(Constant.PRODUCT_NAME));
            status.setStatus(rsStatusDes.getString("STATUS"));
            status.setDescription(rsStatusDes.getString("DESCRIPTION"));
            status.setAllowDelete(rsStatusDes.getInt("ALLOW_DELETED") == 1);
            crmConfig.addStatus(status);
        }
        return crmConfig;
    }

    private Map<String, Set<String>> getProductServiceMap(CallableStatement cs) throws SQLException {
        Map<String, Set<String>> productServiceMap = new HashMap<>();
        ResultSet rs = (ResultSet) cs.getObject("P_CRM_PRODUCT_SERVICE_MAP");
        while (rs.next()) {
            Set<String> services = productServiceMap.computeIfAbsent(rs.getString(Constant.PRODUCT_NAME), k -> new HashSet<>());
            services.add(rs.getString("SERVICE_ID"));
        }
        return productServiceMap;
    }

    private static CrmConfig getCrmConfig(CallableStatement cs, Map<String, Set<String>> productServiceMap) throws SQLException {
        CrmConfig crmConfig = new CrmConfig();
        ResultSet rsCrmConfig = (ResultSet) cs.getObject("P_CRM_PRODUCT_CONFIG");
        while (rsCrmConfig.next()) {
            CrmConfig.Config config = new CrmConfig.Config();
            config.setProductName(rsCrmConfig.getString(Constant.PRODUCT_NAME));
            config.setCallerId(rsCrmConfig.getString("CALLER_ID"));
            config.setServiceIds(productServiceMap.get(config.getProductName()).stream().toList());
            crmConfig.addConfig(config);
        }
        return crmConfig;
    }
}
