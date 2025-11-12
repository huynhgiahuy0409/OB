package com.mservice.fs.onboarding.service;

import com.mservice.fs.app.component.*;
import com.mservice.fs.generic.service.DataCreator;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.onboarding.model.common.config.ConsentConfigDB;
import com.mservice.fs.onboarding.model.common.config.CrmConfig;
import com.mservice.fs.onboarding.model.common.config.FlowConfig;
import com.mservice.fs.onboarding.model.common.config.FormulaDueDateConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiDB;
import com.mservice.fs.onboarding.model.common.config.ObActionTypeConfig;
import com.mservice.fs.onboarding.model.common.config.ObContractConfig;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceGroupConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.config.*;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hoang.thai
 * on 12/3/2023
 */
public class ServiceInfoService extends CallableProcessor<ServiceObConfig> implements DataCreator<ServiceObConfig> {

    @Override
    public ServiceObConfig create() throws Exception, BaseException {
        return run();
    }

    @Override
    protected ServiceObConfig processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ServiceObConfig serviceObConfig = new ServiceObConfig();
        Map<String, LoanActionAiConfig> loanDeciderMap = new HashMap<>();
        Map<String, LoanActionAiConfig> knockOutRuleMap = new HashMap<>();

        loadLoanActionAiConfig(cs, loanDeciderMap, knockOutRuleMap);
        loadServiceConfig(serviceObConfig, cs, loanDeciderMap, knockOutRuleMap);
        loadAiRule(serviceObConfig, cs);
        loadActionConfig(serviceObConfig, cs);
        loadActionField(serviceObConfig, cs);
        loadOtpConfig(serviceObConfig, cs);
        loadFlowConfig(serviceObConfig, cs);
        loadRenderData(serviceObConfig, cs);
        loadFormulaDueDateConfig(serviceObConfig, cs);
        loadServiceGroup(serviceObConfig, cs);
        loadContractConfig(serviceObConfig, cs);
        loadAiMappingConfig(serviceObConfig, cs);
        loadUserConsent(serviceObConfig, cs);
        loadCrmConfig(serviceObConfig, cs);
        loadPartnerConfig(serviceObConfig, cs);
        loadTelcoConfig(serviceObConfig, cs);
        return serviceObConfig;
    }

    private void loadPartnerConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws BaseException, Exception {
        Log.MAIN.info("===========Start load Partner Config============");
        JdbcTransformer.toObjects((ResultSet) cs.getObject("P_PARTNER_CONFIG"), OBPartnerConfig.class, data -> {
            try {
                PartnerConfig partnerConfig = serviceObConfig.getServiceObInfo(data.getServiceId()).loadPartnerConfig(data.getPartnerId());
                partnerConfig.setMiniAppTrackVerPackage(data.getMiniAppTrackVerPackage());
                partnerConfig.setId(data.getPartnerId());
                partnerConfig.setApplyStatusAtFinalSubmit(data.getApplyStatusAtFinalSubmit() == 1);
                partnerConfig.setApplyKnockOutRuleLenderId(data.getApplyKnockOutRuleLenderId() == 1);
                partnerConfig.setLenderIdAI(data.getLenderIdAI());
                partnerConfig.setApplyScoreAtLoanDecider(data.getApplyScoreAtLoanDecider() == 1);
                partnerConfig.setCallerId(data.getCallerId());
                partnerConfig.setApplySendPlatformListener(data.getApplySendPlatformListener() == 1);
                partnerConfig.setApplySendPlatformTask(data.getApplySendPlatformTask() == 1);
            } catch (BaseException e) {
                Log.MAIN.error("Error when load partner config {}", data, e);
                throw new RuntimeException(e);
            }
        });
        Log.MAIN.info("===========End load Partner config============");
    }

    private void loadUserConsent(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        Log.MAIN.info("===========Start load user consent============");
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_CONSENT_CONFIG), ConsentConfigDB.class, data -> {
            ServiceObInfo serviceObInfo;
            try {
                serviceObInfo = serviceObConfig.getServiceObInfo(data.getServiceId());
            } catch (BaseException e) {
                Log.MAIN.error("Error when load consent config with consentData {}", data);
                throw new RuntimeException(e);
            }
            serviceObInfo.addAttributeConsent(data.getAttributeName(), data.getAccessType(), data.getMiniAppId(), data.getPartnerCode());
        });
        Log.MAIN.info("===========End load user consent============");
    }

    private void loadAiMappingConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception, BaseException {
        ResultSet rs = (ResultSet) cs.getObject(Constant.P_AI_MAPPING_CONFIG);

        while (rs.next()) {
            String userProfileInfo = rs.getString(Constant.USER_PROFILE_INFO_COLUMN);
            String loanActionName = rs.getString(Constant.AI_LOAN_ACTION_NAME_COLUMN);
            Integer resultCode = rs.getInt(Constant.RESULT_CODE_COLUMN);
            String processName = rs.getString(Constant.REDIRECT_PROCESS_NAME_COLUMN);

            List<String> userProfileInfos = new ArrayList<>();
            String[] uProfileInfos = userProfileInfo.split(Constant.COMMA_DELIMITER);
            for (String infoName : uProfileInfos) {
                userProfileInfos.add(infoName);
            }
            List<String> aiLoanActionNames = new ArrayList<>();
            String[] loanActionNames = loanActionName.split(Constant.COMMA_DELIMITER);
            for (String name : loanActionNames) {
                aiLoanActionNames.add(name);
            }

            String serviceId = rs.getString(Constant.SERVICE_ID_COLUMN);
            AiLoanActionConfig config = new AiLoanActionConfig();
            config.setServiceId(serviceId);
            config.setUserProfileInfos(userProfileInfos);
            config.setAiLoanActionNames(aiLoanActionNames);
            config.setResultCode(resultCode);
            config.setRedirectProcessName(processName);

            ServiceObInfo serviceObInfo = serviceObConfig.getServiceObInfo(serviceId);
            List<AiLoanActionConfig> aiLoanActionConfigs = serviceObInfo.getAiLoanActionConfigs();
            aiLoanActionConfigs.add(config);

            Set<String> loanActionNameDistinct = serviceObInfo.getLoanActionNameScopeMap().get(serviceId);
            if (Utils.isEmpty(loanActionNameDistinct)) {
                loanActionNameDistinct = new HashSet<>();
            }
            loanActionNameDistinct.addAll(aiLoanActionNames);
            serviceObInfo.getLoanActionNameScopeMap().put(serviceId, loanActionNameDistinct);
        }
    }

    private void loadContractConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_CONTRACT_CONFIG), ObContractConfig.class, data -> {
            try {
                PartnerConfig partnerConfig = serviceObConfig.getServiceObInfo(data.getServiceId()).loadPartnerConfig(data.getPartnerId());
                partnerConfig.addTypeContract(data.getType(), data.getApplyZeroInterest() == 1);
            } catch (BaseException e) {
                Log.MAIN.error("Error when load contract config with serviceId {}", data.getServiceId());
                throw new RuntimeException(e);
            }
        });
    }

    private void loadOtpConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_OTP_CONFIG), OtpConfig.class,
                otpInfo -> {
                    try {
                        PartnerConfig partnerConfig = serviceObConfig.getServiceObInfo(otpInfo.getServiceId()).loadPartnerConfig(otpInfo.getPartnerId());
                        partnerConfig.setOtpConfig(otpInfo);
                    } catch (BaseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void loadServiceGroup(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_SERVICE_GROUP), ServiceGroupConfig.class, serviceGroupConfig -> {
            try {
                serviceObConfig.getServiceObInfo(serviceGroupConfig.getServiceId()).setServiceGroup(serviceGroupConfig.getGroupType());
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadFormulaDueDateConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_FORMULA_DUE_DATE_CONFIG), FormulaDueDateConfig.class, dueDateConfig -> {
            try {
                serviceObConfig.getServiceObInfo(dueDateConfig.getServiceId()).addDueDateConfig(dueDateConfig);
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadRenderData(ServiceObConfig serviceObConfig, CallableStatement cs) throws BaseException, SQLException, IOException {
        ResultSet rs = (ResultSet) cs.getObject(Constant.P_RENDER_DATA);
        while (rs.next()) {
            String id = CommonConstant.STRING_EMPTY;
            try {
                String serviceId = rs.getString(Constant.SERVICE_ID_COLUMN);
                int resultCode = rs.getInt(Constant.RESULT_CODE_COLUMN);
                String processName = rs.getString(Constant.PROCESS_NAME_COLUMN);
                id = String.join(CommonConstant.UNDERSCORE, serviceId, String.valueOf(resultCode), processName);

                RenderData renderData = new RenderData();
                SimplePage simplePage = new SimplePage(RenderType.valueOf(rs.getString(Constant.RENDER_TYPE_COLUMN)));
                simplePage.setTitle(rs.getString(Constant.TITLE_COLUMN));
                simplePage.setMessage(rs.getString(Constant.MESSAGE_COLUMN));
                ButtonDivision buttonDivision = new ButtonDivision();
                buttonDivision.setButtonDirection(ButtonDivision.Direction.valueOf(rs.getString(Constant.BUTTON_DIRECTION_COLUMN)));
                Button primary = null;
                Button secondary = null;
                String image = rs.getString(Constant.IMAGE_COLUMN);
                String primaryString = rs.getString(Constant.PRIMARY_COLUMN);
                String secondaryString = rs.getString(Constant.SECONDARY_COLUMN);
                try {
                    simplePage.setImage(image);
                    if (Utils.isNotEmpty(primaryString)) {
                        primary = JsonUtil.fromString(primaryString, Button.class);
                    }
                    if (Utils.isNotEmpty(secondaryString)) {
                        secondary = JsonUtil.fromString(secondaryString, Button.class);
                    }
                } catch (Exception | ValidatorException e) {
                    Log.MAIN.error("Error when load Image {} primary {} secondary {}", image, primaryString, secondaryString);
                    throw new RuntimeException(e);
                }
                String trackingParams = rs.getString(Constant.TRACKING_PARAMS_COLUMN);
                if (Utils.isNotEmpty(trackingParams)) {
                    Map<String, Object> map = JsonUtil.fromString(trackingParams, Map.class);
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        simplePage.addTrackingData(entry.getKey(), entry.getValue());
                    }
                }
                String navigationType = rs.getString(Constant.NAVIGATION_TYPE_COLUMN);
                if (Utils.isNotEmpty(navigationType)) {
                    simplePage.setNavigationType(NavigationType.valueOf(navigationType));
                }
                buttonDivision.setPrimary(primary);
                buttonDivision.setSecondary(secondary);
                simplePage.setButtons(buttonDivision);
                simplePage.setId(id);
                renderData.setInformationPage(simplePage);
                Log.MAIN.info("Load success Render Data Key: [{}]", serviceId + "_" + resultCode + "_" + processName);
                serviceObConfig.getServiceObInfo(serviceId).addAddRenderData(resultCode, processName, renderData);
            }
            catch (Throwable throwable) {
                Log.MAIN.fatal("[CRITICAL] Error when load render data [{}]: ", id, throwable);
            }
        }
    }

    private void loadFlowConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_FLOW_CONFIG), FlowConfig.class, flowConfig -> {
            try {
                serviceObConfig.getServiceObInfo(flowConfig.getServiceId()).addRedirect(flowConfig);
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadActionField(ServiceObConfig serviceObConfig, CallableStatement cs) throws BaseException, SQLException {
        ResultSet rsField = (ResultSet) cs.getObject(Constant.P_ACTION_FIELD);
        while (rsField.next()) {
            String fieldName = rsField.getString(Constant.FIELD_NAME_COLUMN);
            String serviceId = rsField.getString(Constant.SERVICE_ID_COLUMN);
            String actionName = rsField.getString(Constant.ACTION_ID_COLUMN);
            Action action;
            try {
                action = Action.valueOf(actionName);
            } catch (Exception exception) {
                Log.MAIN.error("Can not load config field with action {}", actionName);
                throw new BaseException(CommonErrorCode.SYSTEM_BUG);
            }
            serviceObConfig.getServiceObInfo(serviceId)
                    .getActionInfo(action, rsField.getString(Constant.PROCESS_NAME_COLUMN))
                    .getAllowFields().add(fieldName);
        }
    }

    private void loadActionConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception, BaseException {
        ResultSet rsActionConfig = (ResultSet) cs.getObject(Constant.P_ACTION_CONFIG);
        while (rsActionConfig.next()) {
            String serviceId = rsActionConfig.getString(Constant.SERVICE_ID_COLUMN);
            String actionId = rsActionConfig.getString(Constant.ACTION_ID_COLUMN);
            Action action;
            try {
                action = Action.valueOf(actionId);
            } catch (Exception exception) {
                Log.MAIN.error("[CRITICAL] Can not load config with action {}", actionId);
                continue;
            }
            ActionInfo actionInfo = new ActionInfo();
            actionInfo.setAction(action);
            actionInfo.setProcessName(rsActionConfig.getString(Constant.PROCESS_NAME_COLUMN));
            serviceObConfig.getServiceObInfo(serviceId).addActionConfig(serviceId, actionInfo);
        }
    }

    private void loadAiRule(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_AI_RULE), AiConfig.class, aiConfig -> {
            try {
                serviceObConfig.getServiceObInfo(aiConfig.getServiceId()).setAiConfig(aiConfig);
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadServiceConfig(ServiceObConfig serviceObConfig, CallableStatement cs, Map<String, LoanActionAiConfig> loanDeciderMap, Map<String, LoanActionAiConfig> knockOutRuleMap) throws SQLException, BaseException {
        ResultSet rsService = (ResultSet) cs.getObject(Constant.P_SERVICE_CONFIG);
        while (rsService.next()) {
            ServiceObInfo serviceObInfo = new ServiceObInfo(rsService.getString(Constant.SERVICE_ID_COLUMN));
            String userTypeKnockOutRule = rsService.getString(Constant.USER_TYPE_KNOCK_OUT_RULE_COLUMN);
            String userTypeGetPackage = rsService.getString(Constant.USER_TYPE_GET_PACKAGE_COLUMN);
            Long pendingFormCacheTimeInMillis = rsService.getLong(Constant.PENDING_FORM_CACHE_TIME_IN_MILLIS_COLUMN);
            Long bannedFormCacheTimeInMillis = rsService.getLong(Constant.BANNED_FORM_CACHE_TIME_IN_MILLIS_COLUMN);
            String applicationStatusHitDedup = rsService.getString(Constant.APPLICATION_STATUS_HIT_DEDUP_COLUMN);
            serviceObInfo.setLoanGoalDefault(rsService.getString(Constant.LOAN_GOAL_DEFAULT));
            if (Utils.isNotEmpty(pendingFormCacheTimeInMillis)) {
                serviceObInfo.setPendingFormCacheTimeInMillis(pendingFormCacheTimeInMillis);
            }

            if (Utils.isNotEmpty(bannedFormCacheTimeInMillis)) {
                serviceObInfo.setBannedFormCacheTimeInMillis(bannedFormCacheTimeInMillis);
            }

            if (Utils.isNotEmpty(userTypeGetPackage)) {
                serviceObInfo.setUserTypeGetPackage(Arrays.stream(userTypeGetPackage.split(","))
                        .map(UserType::valueOf)
                        .collect(Collectors.toSet()));
            }
            if (Utils.isNotEmpty(userTypeKnockOutRule)) {
                serviceObInfo.setUserTypeKnockOutRule(Arrays.stream(userTypeKnockOutRule.split(","))
                        .map(UserType::valueOf)
                        .collect(Collectors.toSet()));
            }

            if (Utils.isNotEmpty(applicationStatusHitDedup)) {
                serviceObInfo.setApplicationStatusHitDedup(Arrays.stream(applicationStatusHitDedup.split(","))
                        .map(ApplicationStatus::valueOf)
                        .collect(Collectors.toSet()));
            }

            String type = rsService.getString(Constant.ACTION_TYPE_COLUMN);
            if (Utils.isNotEmpty(type)) {
                serviceObInfo.setActionType(ObActionTypeConfig.valueOf(type));
            }

            boolean deletedCacheWhenReject = rsService.getInt(Constant.DELETED_CACHE_WHEN_REJECT_COLUMN) == 1;
            serviceObInfo.setDeleteCacheWhenReject(deletedCacheWhenReject);
            boolean deletedCacheWhenOutWhiteList = rsService.getInt(Constant.DELETE_CACHE_OUT_WHITE_LIST) == 1;
            serviceObInfo.setDeleteCacheOutWhiteList(deletedCacheWhenOutWhiteList);
            boolean isGenerateOtpWhenSubmit = rsService.getInt(Constant.IS_GENERATE_OTP_WHEN_SUBMIT) == 1;
            serviceObInfo.setGenerateOtpWhenSubmit(isGenerateOtpWhenSubmit);
            serviceObInfo.setLoanDeciderConfigMap(loanDeciderMap);
            serviceObInfo.setKnockOutRuleConfigMap(knockOutRuleMap);

            boolean isCalculateDueDate = !Utils.isEmpty(rsService.getInt(Constant.IS_CALCULATE_DUE_DATE_COLUMN)) && rsService.getInt(Constant.IS_CALCULATE_DUE_DATE_COLUMN) > 0;
            serviceObInfo.setDoCalculateDueDate(isCalculateDueDate);

            String serviceName = rsService.getString(Constant.SERVICE_NAME_COLUMN);
            serviceObInfo.setServiceName(serviceName);

            boolean isAiActionMapping = !Utils.isEmpty(rsService.getInt(Constant.AI_ACTION_MAPPING_COLUMN)) && rsService.getInt(Constant.AI_ACTION_MAPPING_COLUMN) > 0;
            serviceObInfo.setAiActionMapping(isAiActionMapping);

            boolean isAiActionMappingLD = !Utils.isEmpty(rsService.getInt(Constant.AI_ACTION_MAPPING_LD_COLUMN)) && rsService.getInt(Constant.AI_ACTION_MAPPING_LD_COLUMN) > 0;
            serviceObInfo.setAiActionMappingLD(isAiActionMappingLD);

            serviceObInfo.setTimeRemindUser(rsService.getInt(Constant.TIME_REMIND_USER));
            serviceObInfo.setCallerId(rsService.getString(Constant.CALLER_ID));
            serviceObInfo.setContractLength(rsService.getInt(Constant.CONTRACT_LENGTH));
            serviceObInfo.setScoreServiceIdRisk(rsService.getString(Constant.SCORE_SERVICE_ID_RISK));
            serviceObInfo.setScoreMsgTypeRisk(rsService.getString(Constant.SCORE_MSG_TYPE_RISK));
            serviceObInfo.setApplyZeroInterest(rsService.getInt(Constant.ZERO_INTEREST) == 1);

            boolean isRecheckPendingForm = !Utils.isEmpty(rsService.getInt(Constant.RECHECK_PENDING_FORM)) && rsService.getInt(Constant.RECHECK_PENDING_FORM) > 0;
            serviceObInfo.setRecheckPendingForm(isRecheckPendingForm);


            String serviceMerge = rsService.getString(Constant.SERVICE_MERGE);

            if (Utils.isNotEmpty(serviceMerge)) {
                serviceObInfo.setServiceMerge(Arrays.stream(serviceMerge.split(","))
                        .collect(Collectors.toList()));
            }
            String statusAllowCancelByLender = rsService.getString(Constant.STATUS_ALLOW_CANCEL_BY_LENDER);
            if (Utils.isNotEmpty(statusAllowCancelByLender)) {
                serviceObInfo.setStatusAllowCancelByLender(Arrays.stream(statusAllowCancelByLender.split(","))
                        .map(ApplicationStatus::valueOf)
                        .collect(Collectors.toSet()));
            }

            String statusAllowRejectByLender = rsService.getString(Constant.STATUS_ALLOW_REJECT_BY_LENDER);
            if (Utils.isNotEmpty(statusAllowRejectByLender)) {
                serviceObInfo.setStatusAllowRejectByLender(Arrays.stream(statusAllowRejectByLender.split(","))
                        .map(ApplicationStatus::valueOf)
                        .collect(Collectors.toSet()));
            }

            boolean isReapplyWhenLenderReject = !Utils.isEmpty(rsService.getInt(Constant.REAPPLY_WHEN_LENDER_REJECT)) && rsService.getInt(Constant.REAPPLY_WHEN_LENDER_REJECT) > 0;
            serviceObInfo.setReapplyWhenLenderReject(isReapplyWhenLenderReject);

            serviceObConfig.addServiceObInfo(serviceObInfo);
        }
    }

    private void loadLoanActionAiConfig(CallableStatement cs, Map<String, LoanActionAiConfig> loanDeciderMap, Map<String, LoanActionAiConfig> knockOutRuleMap) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_LOAN_ACTION_AI_CONFIG), LoanActionAiDB.class, data ->
        {
            try {
                LoanActionAiConfig loanActionAiConfig = new LoanActionAiConfig();
                loanActionAiConfig.setActionName(data.getActionName());
                loanActionAiConfig.setResultCode(data.getResultCode());
                loanActionAiConfig.setRedirectProcessName(data.getRedirectProcessName());
                loanActionAiConfig.setAiTypeConfig(data.getType());
                switch (data.getType()) {
                    case LOAN_DECIDER:
                        loanDeciderMap.put(loanActionAiConfig.getActionName(), loanActionAiConfig);
                        break;
                    case KNOCK_OUT_RULE:
                        knockOutRuleMap.put(loanActionAiConfig.getActionName(), loanActionAiConfig);
                        break;
                    default:
                        throw new RuntimeException("Data Type LoanAction Ai config not found" + data.getType());
                }
            } catch (Exception e) {
                Log.MAIN.error("Error When set ServiceConfig", e);
                throw new RuntimeException(e);
            }

        });
    }

    private void loadCrmConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        JdbcTransformer.toObjects((ResultSet) cs.getObject(Constant.P_CRM_CONFIG), CrmConfig.class, crmConfig -> {
            try {
                serviceObConfig.getServiceObInfo(crmConfig.getServiceId()).addCrmConfig(crmConfig);
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadTelcoConfig(ServiceObConfig serviceObConfig, CallableStatement cs) throws Exception {
        Log.MAIN.info("===========Start load Telco Config============");
        JdbcTransformer.toObjects((ResultSet) cs.getObject("P_TELCO_CONFIG"), TelcoConfig.class, data -> {
            try {
                ServiceObInfo serviceObInfo = serviceObConfig.getServiceObInfo(data.getServiceId());
                serviceObInfo.setTelcoConfig(data);
            } catch (BaseException e) {
                Log.MAIN.error("Error when load telco config {}", data, e);
                throw new RuntimeException(e);
            }
        });
        Log.MAIN.info("===========End load Partner config============");
    }
}
