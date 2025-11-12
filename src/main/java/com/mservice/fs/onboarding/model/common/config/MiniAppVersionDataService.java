package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.AppMetadata;
import com.mservice.fs.model.DefaultRequest;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.processor.JobData;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MiniAppVersionDataService {
    private Map<String, MiniAppVersionConfig> miniAppVersions = new HashMap<>();
    private Map<String, MiniAppVersionConfig> miniAppVersionsByAction = new HashMap<>();


    public void addAppVersion(MiniAppVersionConfig config) {
        if (Utils.isNotEmpty(config) && Utils.isNotEmpty(config.getResultCode())) {
            miniAppVersions.put(config.toString(), config);
        }
        if (Utils.isNotEmpty(config) && Utils.isNotEmpty(config.getAction())) {
            miniAppVersionsByAction.put(config.buildActionKey(), config);
        }
    }

    public boolean isMiniAppVersionNotSupport(JobData<? extends DefaultRequest, ?> jobData, ErrorCode errorCode) {
        Base base = jobData.getBase();
        DefaultRequest request = jobData.getRequest();
        String key = MiniAppVersionConfig.createKey(base.getServiceId(), base.getProcessName(), errorCode.getCode());
        return isMiniAppVersionNotSupport(jobData, request, key, miniAppVersions);
    }

    private boolean isMiniAppVersionNotSupport(JobData<? extends DefaultRequest, ?> jobData, DefaultRequest request, String key, Map<String, MiniAppVersionConfig> miniAppVersions) {
        if (miniAppVersions.containsKey(key)) {
            MiniAppVersionConfig miniAppVersionConfig = miniAppVersions.get(key);
            AppMetadata metadata = request.getMetadata();
            if (metadata == null || metadata.getMiniAppTrackVer() < miniAppVersionConfig.version) {
                Integer currentMiniAppVer = null;
                if (metadata != null) {
                    currentMiniAppVer = metadata.getMiniAppTrackVer();
                }
                Log.MAIN.info("MiniApp Version not support for initiator: {} || with key: {} || current version: {} || required version support: {}", jobData.getInitiator(), key, currentMiniAppVer, miniAppVersionConfig.version);
                return true;
            }
        }
        return false;
    }

    public boolean isMiniAppVersionNotSupportAction(JobData<? extends DefaultRequest, ?> jobData, String action) {
        Base base = jobData.getBase();
        DefaultRequest request = jobData.getRequest();
        String key = MiniAppVersionConfig.createActionKey(base.getServiceId(), base.getProcessName(), action);
        return isMiniAppVersionNotSupport(jobData, request, key, miniAppVersionsByAction);
    }

    @Getter
    public static class MiniAppVersionConfig {
        private String serviceId;
        private String processName;
        private Integer resultCode;
        private Integer version;
        private String action;

        @Override
        public String toString() {
            return createKey(serviceId, processName, resultCode);
        }

        public static String createKey(String serviceId, String processName, Integer resultCode) {
            return String.join(CommonConstant.UNDERSCORE, serviceId, processName, resultCode.toString());
        }

        public static String createActionKey(String serviceId, String processName, String action) {
            return String.join(CommonConstant.UNDERSCORE, serviceId, processName, action);
        }

        public String buildActionKey() {
            return createActionKey(serviceId, processName, action);
        }
    }


}
