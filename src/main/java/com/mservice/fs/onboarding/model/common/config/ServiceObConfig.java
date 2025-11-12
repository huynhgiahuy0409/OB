package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.utils.Utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hoang.thai
 * on 12/3/2023
 */
public class ServiceObConfig {

    private Map<String, ServiceObInfo> serviceAiInfoMap = new ConcurrentHashMap<>();

    public ServiceObInfo getServiceObInfo(String serviceId) throws BaseException {
        if (Utils.isEmpty(serviceId)) {
            Log.MAIN.error("Cannot get ServiceObInfo with empty serviceId: [{}]", serviceId);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
        ServiceObInfo serviceObInfo = serviceAiInfoMap.get(serviceId);
        if (serviceObInfo == null) {
            Log.MAIN.error("ServiceObInfo [{}] is null", serviceId);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
        return serviceObInfo;
    }

    public void addServiceObInfo(ServiceObInfo serviceObInfo) throws BaseException {
        String serviceId = serviceObInfo.getServiceId();
        if (Utils.isEmpty(serviceId)) {
            Log.MAIN.error("Cannot add ServiceAiInfo with empty serviceId: [{}]", serviceId);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
        this.serviceAiInfoMap.put(serviceId, serviceObInfo);
    }

    public String getCallerId(String serviceId, String partnerId) throws BaseException {
        ServiceObInfo serviceObInfo = getServiceObInfo(serviceId);
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(partnerId);
        if(Utils.isEmpty(partnerConfig) || Utils.isEmpty(partnerConfig.getCallerId())){
            return serviceObInfo.getCallerId();
        }
        return partnerConfig.getCallerId();
    }

    public ServiceObInfo getServiceObInfoByLoanProductCode(String loanProductCode) throws BaseException {
        if (Utils.isEmpty(loanProductCode)) {
            Log.MAIN.error("Cannot get ServiceObInfo with empty loanProductCode: [{}]", loanProductCode);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
        Optional<ServiceObInfo> serviceObInfoOptional = serviceAiInfoMap.values().stream()
                .filter(serviceObInfo -> Utils.isNotEmpty(serviceObInfo.getAiConfig()))
                .filter(serviceObInfo -> loanProductCode.equals(serviceObInfo.getAiConfig().getLoanProductCode()))
                .findFirst();
        if (serviceObInfoOptional.isEmpty()) {
            throw new BaseException(CommonErrorCode.INVALID_REQUEST);
        }
        return serviceObInfoOptional.get();
    }
}
