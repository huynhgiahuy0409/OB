package com.mservice.fs.onboarding.model.crm;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CrmConfig {

    private Map<String, Config> configMap = new HashMap<>();

    public void addConfig(Config config) {
        configMap.put(config.getProductName(), config);
    }

    public void addStatus(Status status) {
        configMap.get(status.getProductName()).addStatus(status);
    }

    @Getter
    @Setter
    public static class Config {
        private String productName;
        private List<String> serviceIds;
        private String callerId;
        private Map<String, Status> applicationStatusMap = new HashMap<>();

        public void addStatus(Status status) {
            applicationStatusMap.put(status.getStatus(), status);
        }

        public boolean isAllowDeleted(String status) {
            return applicationStatusMap.get(status).isAllowDelete();
        }
    }

    @Getter
    @Setter
    public static class Status {
        private String productName;
        private String status;
        private String description;
        private boolean allowDelete;
    }
}
