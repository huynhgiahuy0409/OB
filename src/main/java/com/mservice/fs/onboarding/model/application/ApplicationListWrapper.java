package com.mservice.fs.onboarding.model.application;

import com.mservice.fs.json.CacheObject;
import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.submit.ScamAlertResult;
import com.mservice.fs.onboarding.model.common.ai.UserActionEvent;
import com.mservice.fs.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hoang.thai
 * on 11/14/2023
 */
@Getter
@Setter
public class ApplicationListWrapper implements CacheObject {

    private static final String NAME = "APPLICATION";

    private List<ApplicationForm> applicationForms;
    private UserActionEvent userActionEvent;

    public ApplicationListWrapper() {
        applicationForms = new ArrayList<>();
    }

    @Override
    public String toCacheString() throws Exception {
        return JsonUtil.toString(this);
    }

    @Override
    public String toString() {
        return Json.encodeHiddenFields(this);
    }

    public static String createKey(String serviceId, String agentId) {
        return NAME + ":" + serviceId + "_" + agentId;
    }

    public static String createKeyNotify(String serviceId, String agentId, String applicationId) {
        return serviceId + "_" + agentId + "_" + applicationId;
    }

    public void buildDataForNotify(String applicationId) {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setApplicationId(applicationId);
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setApplicationData(applicationData);
        applicationForms = List.of(applicationForm);
    }

    public ApplicationForm getApplicationByPartner(String partnerId) {
        for (ApplicationForm applicationForm : applicationForms) {
            if (applicationForm.getApplicationData().getPartnerId().equals(partnerId)) {
                return applicationForm;
            }
        }
        return null;
    }

    public ApplicationForm getApplicationById(String applicationId) {
        for (ApplicationForm applicationForm : applicationForms) {
            if (applicationForm.getApplicationData().getApplicationId().equals(applicationId)) {
                return applicationForm;
            }
        }
        return null;
    }
}
