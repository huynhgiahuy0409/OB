package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.app.component.RenderData;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hoang.thai
 * on 12/3/2023
 */
@Getter
@Setter
public class ServiceObInfo {

    private String serviceId;
    private Set<UserType> userTypeKnockOutRule;
    private Set<UserType> userTypeGetPackage;
    private Map<String, ActionInfo> actionInfoMap;
    private AiConfig aiConfig;
    private Long pendingFormCacheTimeInMillis;
    private Long bannedFormCacheTimeInMillis;
    private boolean deleteCacheWhenReject;
    private Map<String, String> flowMap;
    private Map<String, RenderData> renderDataMap;
    private ObActionTypeConfig actionType;
    private Map<String, LoanActionAiConfig> loanDeciderConfigMap;
    private Map<String, LoanActionAiConfig> knockOutRuleConfigMap;
    private Map<String, PartnerConfig> partnerMap;
    private boolean doCalculateDueDate;
    private List<FormulaDueDateConfig> formulaDueDateConfig;
    private Set<ApplicationStatus> applicationStatusHitDedup;
    private String serviceGroup;
    private String serviceName;
    private boolean deleteCacheOutWhiteList;
    private boolean aiActionMapping;
    private boolean aiActionMappingLD;
    private List<AiLoanActionConfig> aiLoanActionConfigs;
    private Map<String, Set<String>> loanActionNameScopeMap;
    private Integer timeRemindUser;
    private String loanGoalDefault;
    private ConsentConfig consentConfig;
    private boolean generateOtpWhenSubmit;
    private List<CrmConfig> crmConfig;
    private String callerId;
    private String scoreServiceIdRisk;
    private String scoreMsgTypeRisk;
    private boolean applyZeroInterest;
    private Integer contractLength;
    private TelcoConfig telcoConfig;
    private boolean recheckPendingForm;
    private List<String> serviceMerge;
    private Set<ApplicationStatus> statusAllowCancelByLender;
    private Set<ApplicationStatus> statusAllowRejectByLender;
    private boolean reapplyWhenLenderReject;


    public ServiceObInfo(String serviceId) {
        this.serviceId = serviceId;
        actionInfoMap = new HashMap<>();
        userTypeKnockOutRule = new HashSet<>();
        userTypeGetPackage = new HashSet<>();
        flowMap = new HashMap<>();
        renderDataMap = new HashMap<>();
        loanDeciderConfigMap = new HashMap<>();
        knockOutRuleConfigMap = new HashMap<>();
        pendingFormCacheTimeInMillis = 1800000L;
        bannedFormCacheTimeInMillis = 1800000L;
        formulaDueDateConfig = new ArrayList<>();
        applicationStatusHitDedup = new HashSet<>();
        partnerMap = new HashMap<>();
        aiLoanActionConfigs = new ArrayList<>();
        loanActionNameScopeMap = new HashMap<>();
        consentConfig = new ConsentConfig();
        crmConfig = new ArrayList<>();
        serviceMerge = new ArrayList<>();
    }

    public void addActionConfig(String serviceId, ActionInfo actionInfo) {
        actionInfoMap.put(createKeyActionConfig(serviceId, actionInfo.getAction(), actionInfo.getProcessName()), actionInfo);
    }

    public void addDueDateConfig(FormulaDueDateConfig dueDateConfig) {
        formulaDueDateConfig.add(dueDateConfig);
    }

    public String createKeyActionConfig(String serviceId, Action action, String processName) {
        return String.format("%s/%s/%s", serviceId, action.name(), processName);
    }

    public ActionInfo getActionInfo(Action action, String process) {
        return actionInfoMap.get(createKeyActionConfig(serviceId, action, process));
    }

    public boolean isMatchAction(Action action, String processName) {
        ActionInfo actionInfo = actionInfoMap.get(createKeyActionConfig(this.serviceId, action, processName));
        if (actionInfo != null) {
            Log.MAIN.info("Match Action {} with ServiceId {} - processName {} ", action.name(), this.serviceId, processName);
            return true;
        }
        Log.MAIN.info("Not match Action {} with ServiceId {} - processName {} ", action.name(), this.serviceId, processName);
        return false;
    }

    public boolean isNotMatchAction(Action action, String processName) {
        return !isMatchAction(action, processName);
    }

    public void addRedirect(FlowConfig flowConfig) {
        flowMap.put(flowConfig.getProcessName(), flowConfig.getNextProcessName());
    }

    public String getNextDirection(String processName) {
        return flowMap.get(processName);
    }

    public void addAddRenderData(int resultCode, String processName, RenderData renderData) throws BaseException {
        if (Utils.isEmpty(this.serviceId) || Utils.isEmpty(resultCode) || Utils.isEmpty(processName)) {
            Log.MAIN.error("Cannot Load Render data with serviceId {} or resultCode empty {} or processName {}", this.serviceId, resultCode, processName);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
        this.renderDataMap.put(getKeyRenderData(this.serviceId, resultCode, processName), renderData);
    }

    public String getKeyRenderData(String serviceId, int resultCode, String processName) {
        return String.format("%s_%s_%s", serviceId, resultCode, processName);
    }

    public RenderData getRenderData(int resultCode, String processName) {
        Log.MAIN.info("Get Render data with serviceId {} - resultCode empty {} - processName {}", this.serviceId, resultCode, processName);
        return renderDataMap.get(getKeyRenderData(this.serviceId, resultCode, processName));
    }

    public PartnerConfig loadPartnerConfig(String partnerId) throws BaseException {
        PartnerConfig partnerConfig = partnerMap.get(partnerId);
        if (partnerConfig == null) {
            partnerMap.put(partnerId, new PartnerConfig());
        }
        return partnerMap.get(partnerId);
    }

    public PartnerConfig getPartnerConfig(String partnerId) throws BaseException {
        Log.MAIN.info("Get partner config with partnerId {}", partnerId);
        return partnerMap.get(partnerId);
    }

    public void addAttributeConsent(String attributeName, String accessType, String miniAppId, String partnerCode) {
        getConsentConfig().addAttribute(attributeName, accessType, miniAppId, serviceId, partnerCode);
    }

    public void addCrmConfig(CrmConfig crmConfig) {
        this.crmConfig.add(crmConfig);
    }


}
