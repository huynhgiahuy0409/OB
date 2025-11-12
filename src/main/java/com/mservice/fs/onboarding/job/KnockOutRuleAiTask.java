package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.http.KnockOutRuleService;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.enums.AiUserProfileEnumMap;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.enums.UserProfileConfigValue;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.LoanActionType;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.ai.LenderId;
import com.mservice.fs.onboarding.model.ai.ScreenId;
import com.mservice.fs.onboarding.model.application.C06Result;
import com.mservice.fs.onboarding.model.application.KycDataAI;
import com.mservice.fs.onboarding.model.application.NfcInfo;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.onboarding.model.application.UserProfileAI;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleRequest;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockoutRuleTracking;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.SummaryScreenLog;
import com.mservice.fs.onboarding.model.common.config.AIActionMappingConfig;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.FaceMatching;
import com.mservice.fs.sof.queue.model.profile.Identify;
import com.mservice.fs.sof.queue.model.profile.KycC06Verified;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.template.TemplateMessage;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author hoang.thai on 10/30/2023
 */
public abstract class KnockOutRuleAiTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "KNOCK_OUT_RULE-AI";
    protected static final int DEFAULT_STATUS = 1;
    protected static final int DEFAULT_LENDING_FLOW = 1;
    protected static final String NO_ACTION_VALUE = "NO_ACTION";
    protected static final String HIT_ACTION_KOR = "HIT_ACTION_KOR";
    protected static final String HIT_NFC = "HIT_NFC";
    protected static final String SERVICE_ID_NEWTON = "credit_apple_multilender";

    private final Class<?> responseClass;

    @Autowire(name = "KnockOutRule")
    private KnockOutRuleService knockOutRuleService;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire
    private TemplateMessage templateMessage;

    public KnockOutRuleAiTask() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(this.getClass(), OnboardingResponse.class);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        UserProfileInfo userProfileInfo = getUserProfileInfo(jobData);
        ServiceObInfo serviceObInfo = getServiceObInfo(jobData, onboardingDataInfo);
        if (validate(jobData, serviceObInfo) && serviceObInfo.isMatchAction(Action.CHECK_KNOCK_OUT_RULE, jobData.getProcessName())) {
            KnockOutRuleResponse packageResponse = getKnockOutRuleResponse(userProfileInfo, jobData, serviceObInfo, taskData);
            processWithResponse(taskData, packageResponse, jobData);
            validateKnockOutRuleResponse(taskData, packageResponse, jobData, serviceObInfo, userProfileInfo);
        }
        finish(jobData, taskData);
    }

    protected KnockOutRuleResponse getKnockOutRuleResponse(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, TaskData taskData) throws Exception, BaseException {
        String partnerRequest = createPartnerRequest(userProfileInfo, jobData, jobData.getRequest(), serviceObInfo).encode();
        return knockOutRuleService.callApi(partnerRequest, jobData.getBase());
    }

    protected void processWithResponse(TaskData taskData, KnockOutRuleResponse packageResponse, OnboardingData<T, R> jobData) {
    }

    protected ServiceObInfo getServiceObInfo(OnboardingData<T, R> jobData, DataService<ServiceObConfig> onboardingDataInfo) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
    }

    protected boolean validate(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo) {
        return true;
    }

    protected UserProfileInfo getUserProfileInfo(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
    }

    protected KnockOutRuleRequest createPartnerRequest(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, T request, ServiceObInfo serviceObInfo) throws Exception, BaseException {

        long currentTimeInMillis = System.currentTimeMillis();


        String momoApplicationId = CommonConstant.STRING_EMPTY;
        if (SERVICE_ID_NEWTON.equalsIgnoreCase(serviceObInfo.getServiceId())) {
            ApplicationForm applicationForm = getApplicationForm(jobData);
            applicationForm.getApplicationData().getPartnerApplicationId();
        }

        // ---- profile ----
        UserProfileAI userProfileAI = new UserProfileAI();
        userProfileAI.setUserName(userProfileInfo.getFullNameKyc());
        userProfileAI.setNationalIdNumber(userProfileInfo.getPersonalIdKyc());

        if (Utils.isEmpty(userProfileInfo.getIdCardTypeDetailKyc())) {
            userProfileAI.setIdCardType(SummaryScreenLog.IdCardType.UNKNOWN_ID_CARD_TYPE.name());
        } else {
            SummaryScreenLog.IdCardType aiIdCardType = AiUserProfileEnumMap.idCardTypeMap.get(userProfileInfo.getIdCardTypeDetailKyc());
            if (aiIdCardType != null) {
                userProfileAI.setIdCardType(aiIdCardType.name());
            } else {
                userProfileAI.setIdCardType(SummaryScreenLog.IdCardType.OTHER_TYPE.name());
            }
        }

        userProfileAI.setExpireDate(userProfileInfo.getExpiredDateKyc());
        userProfileAI.setUserDob(userProfileInfo.getDobKyc());
        userProfileAI.setIssueDate(userProfileInfo.getIssueDateKyc());
        if (Utils.isNotEmpty(userProfileInfo.getGenderKyc())) {
            userProfileAI.setGender(AiUserProfileEnumMap.genderMap.get(userProfileInfo.getGenderKyc()).getCode());
        }
        userProfileAI.setPhoneNumber(jobData.getInitiator());
        if (Utils.isNotEmpty(AiUserProfileEnumMap.kycStatusMap.get(userProfileInfo.getIdentify()))) {
            userProfileAI.setKycConfirmStatus(AiUserProfileEnumMap.kycStatusMap.get(userProfileInfo.getIdentify()).name());
        }
        if (Utils.isNotEmpty(AiUserProfileEnumMap.faceStatusMap.get(userProfileInfo.getFaceMatching()))) {
            userProfileAI.setFaceMatchingStatus(AiUserProfileEnumMap.faceStatusMap.get(userProfileInfo.getFaceMatching()).name());
        }
        userProfileAI.setFrontImagePath(userProfileInfo.getIdFrontImageKyc());
        userProfileAI.setBackImagePath(userProfileInfo.getIdBackImageKyc());
        userProfileAI.setIssuePlace(userProfileInfo.getIssuePlaceKyc());
        userProfileAI.setAddress(userProfileInfo.getAddressKyc());
        userProfileAI.setLatestKycTimestamp(userProfileInfo.getTimestampKyc());
        userProfileAI.setLatestFaceMatchingTimestamp(userProfileInfo.getFaceMatchTimestamp());
        userProfileAI.setSubBankCode(userProfileInfo.getSubBankCode());
        userProfileAI.setKycC06Verified(getC06VerifiedKycValue(userProfileInfo));
        try {
            Long createTime = Long.parseLong(userProfileInfo.getCreateDate());
            userProfileAI.setWalletCreatedTime(createTime);
        } catch (NumberFormatException e) {
            // skip
            Log.MAIN.info("Parse number error: [{}]", userProfileInfo.getCreateDate());
        }
        //nfc
        NfcInfo nfcInfo = new NfcInfo();
        nfcInfo.setSod(userProfileInfo.getDocSecurityObj());
        nfcInfo.setDg1(userProfileInfo.getDataGroup1());
        nfcInfo.setDg2(userProfileInfo.getDataGroup2());
        nfcInfo.setDg13(userProfileInfo.getDataGroup13());
        nfcInfo.setDg14(userProfileInfo.getDataGroup14());
        nfcInfo.setDg15(userProfileInfo.getDataGroup15());
        userProfileAI.setNfcInfo(nfcInfo);

        //c06
        C06Result c06Result = new C06Result();
        c06Result.setC06TimeVerify(userProfileInfo.getC06TimeVerify());
        c06Result.setKycSignature(userProfileInfo.getKycSignature());
        c06Result.setKycChallenge(userProfileInfo.getKycChallenge());
        c06Result.setKycAAResult(userProfileInfo.getKycAAResult());
        c06Result.setKycEACCAResult(userProfileInfo.getKycEACCAResult());
        c06Result.setKycC06Partner(userProfileInfo.getKycC06Partner());
        userProfileAI.setC06Result(c06Result);

        //merchant
        userProfileAI.setMerchantName(userProfileInfo.getMerchantName());
        userProfileAI.setMerchantAutoCashout(userProfileInfo.isMerchantAutoCashout());
        userProfileAI.setM4bFlag(userProfileInfo.getM4bFlag());

        // ---- profile ----

        // ---- startScreenLog ----
        ScreenLog startScreenLog = new ScreenLog();
        // timestamp
        startScreenLog.setTimestamp(currentTimeInMillis);
        // miniAppVersion
        startScreenLog.setMiniAppVersion((String) jobData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER));
        // screenId
        startScreenLog.setScreenId(ScreenId.START_SCREEN);
        // messageType
        startScreenLog.setMessageType(AIMessageType.START_SCREEN_LOG);
        // loanAppStatus
        startScreenLog.setLoanStatus(DEFAULT_STATUS);
        // lendingFlow
        startScreenLog.setLendingFlow(DEFAULT_LENDING_FLOW);

        setDeviceInfo(startScreenLog, request);

        KycDataAI kycDataAI = new KycDataAI();
        kycDataAI.setFace_selfie_image_s3_path(userProfileInfo.getImageFaceMatching());

        startScreenLog.setKycData(kycDataAI);
        startScreenLog.setProfile(userProfileAI);

        KnockOutRuleRequest knockOutRuleRequest = new KnockOutRuleRequest();
        knockOutRuleRequest.setAppSessionId(Utils.isEmpty(jobData.getBase().getHeaders().get(Constant.MOMO_SESSION_KEY)) ? CommonConstant.STRING_EMPTY : jobData.getBase().getHeaders().get(Constant.MOMO_SESSION_KEY).toString());
        // requestId
        knockOutRuleRequest.setRequestId(request.getRequestId() + "_" + jobData.getTraceId());
        // requestTimestamp
        knockOutRuleRequest.setRequestTimestamp(currentTimeInMillis);
        knockOutRuleRequest.setAgentId(Integer.parseInt(jobData.getInitiatorId()));
        knockOutRuleRequest.setProductGroup(serviceObInfo.getAiConfig().getProductGroup());
        knockOutRuleRequest.setLoanProductCode(serviceObInfo.getAiConfig().getLoanProductCode());
        knockOutRuleRequest.setProductId(serviceObInfo.getAiConfig().getProductId());
        knockOutRuleRequest.setMerchantId(serviceObInfo.getAiConfig().getMerchantId());
        knockOutRuleRequest.setSourceId(serviceObInfo.getAiConfig().getSourceId());
        // momoLoanAppId
        knockOutRuleRequest.setMomoLoanAppId(momoApplicationId);
        // modelVersion
        // messageType
        knockOutRuleRequest.setMessageType(AIMessageType.START_SCREEN_LOG);
        knockOutRuleRequest.setStartScreenLog(startScreenLog);
        knockOutRuleRequest.setLenderId(LenderId.UNKNOWN_LENDER);
        return knockOutRuleRequest;
    }

    protected void validateKnockOutRuleResponse(TaskData taskData, KnockOutRuleResponse knockOutRuleResponse, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo) throws BaseException, Exception {
        KnockoutRuleTracking knockoutRuleTracking = new KnockoutRuleTracking();
        taskData.setResponse(knockoutRuleTracking);
        LoanDeciderRecord loanDeciderRecord = knockOutRuleResponse.getLoanDeciderRecord();
        if (Utils.isEmpty(loanDeciderRecord)) {
            knockoutRuleTracking.setStatus("Call Knock out rule, loanDeciderRecord is null");
            Log.MAIN.error("Call Knock out rule, loanDeciderRecord is null");
            throw new BaseException(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
        }
        AIStatus loanDeciderStatus = loanDeciderRecord.getLoanDeciderStatus();
        R response = (R) Generics.createObject(this.responseClass);
        switch (loanDeciderStatus) {
            case REJECT:
                knockoutRuleTracking.setStatus("Knock out rule REJECT");
                Log.MAIN.info("Knock out rule REJECT");
                response.setResultCode(OnboardingErrorCode.KNOCK_OUT_RULE_REJECT);
                knockoutRuleTracking.setResultCode(response.getResultCode());
                fillResponseDataWhenReject(response, jobData);
                jobData.setResponse(response);
                addTaskData(taskData, knockoutRuleTracking);
                break;
            case APPROVE:
                knockoutRuleTracking.setStatus("Knock out rule APPROVE");
                Log.MAIN.info("Knock out rule APPROVE");
                if (isCheckLoanAction(jobData, serviceObInfo)) {
                    knockoutRuleTracking.activeCheckLoanAction();
                    Log.MAIN.info("Check Loan Action Knock out rule");
                    checkActionWhenApprove(taskData, serviceObInfo, knockOutRuleResponse, userProfileInfo, jobData, response, knockoutRuleTracking);
                }
                addTaskData(taskData, knockoutRuleTracking);
                break;
            default:
                Log.MAIN.error("LoanDecider status is invalid {}", loanDeciderStatus);
                knockoutRuleTracking.setStatus("LoanDecider status is invalid: " + loanDeciderStatus);
                addTaskData(taskData, knockoutRuleTracking);
                throw new BaseException(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
        }
    }

    protected boolean isCheckLoanAction(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo) {
        return true;
    }

    protected void checkActionWhenApprove(TaskData taskData, ServiceObInfo serviceObInfo, KnockOutRuleResponse knockOutRuleResponse, UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, R response, KnockoutRuleTracking knockoutRuleTracking) throws BaseException, Exception {
        LoanDeciderRecord loanDeciderRecord = knockOutRuleResponse.getLoanDeciderRecord();
        setDataMetricKor(jobData, loanDeciderRecord);
        knockoutRuleTracking.setLoanDeciderRecords(loanDeciderRecord.getLoanAction());
        LoanAction loanAction = LoanDeciderRecord.findFirstLoanAction(knockOutRuleResponse.getLoanDeciderRecord());
        if (loanAction == null) {
            knockoutRuleTracking.setMessage("Loan Action AI is empty, Do not Check LoanAction");
            Log.MAIN.info("Do not check LoanAction");
            return;
        }
        ApplicationForm applicationForm = getApplicationForm(jobData);
        if (!serviceObInfo.isAiActionMapping()) {
            Log.MAIN.info("Check loan action {}", loanAction);
            knockoutRuleTracking.setMessage("AI Action Mapping Is False, Check Loan Action");
            LoanActionAiConfig knockOutRuleConfig = serviceObInfo.getKnockOutRuleConfigMap().get(loanAction.getActionName());
            response = OnboardingUtils.createAiRuleResponse(responseClass, knockOutRuleConfig, jobData, applicationForm);
            jobData.setResponse(response);
            knockoutRuleTracking.setResultCode(response.getResultCode());
            return;
        }
        knockoutRuleTracking.activeAiActionMapping();
        AiLoanActionConfig config = mapAiAction(jobData.getBase(), serviceObInfo, knockOutRuleResponse, userProfileInfo, knockoutRuleTracking);
        if (config == null) {
            response.setResultCode(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
            jobData.setResponse(response);
            knockoutRuleTracking.setResultCode(response.getResultCode());
            return;
        }
        jobData.putProcessNameToTemPlateModel(config.getRedirectProcessName());
        response.setResultCode(config.getResultCode());
        knockoutRuleTracking.setResultCode(response.getResultCode());
        addResponse(taskData, jobData, response, config);
    }

    protected void addResponse(TaskData taskData, OnboardingData<T, R> jobData, R response, AiLoanActionConfig aiLoanActionConfig) {
        jobData.setResponse(response);
    }


    protected AiLoanActionConfig mapAiAction(Base base, ServiceObInfo serviceObInfo, KnockOutRuleResponse knockOutRuleResponse, UserProfileInfo userProfileInfo, KnockoutRuleTracking knockoutRuleTracking) {
        LoanDeciderRecord loanDeciderRecord = knockOutRuleResponse.getLoanDeciderRecord();

        Set<String> actionNamesDistinct = serviceObInfo.getLoanActionNameScopeMap().get(serviceObInfo.getServiceId());
        List<LoanAction> loanActionNotConfig = new ArrayList<>();
        List<String> aiActionNames = new ArrayList<>();
        boolean isMapBank = true;

        for (LoanAction aiLoanAction : loanDeciderRecord.getLoanAction()) {
            if (aiLoanAction.getActionName().equals(LoanActionType.RE_MAP_BANK.name())) {
                isMapBank = false;
                continue;
            }
            if (!actionNamesDistinct.contains(aiLoanAction.getActionName())) {
                loanActionNotConfig.add(aiLoanAction);
            } else {
                aiActionNames.add(aiLoanAction.getActionName());
            }
        }

        if (Utils.isNotEmpty(loanActionNotConfig)) {
            Log.MAIN.fatal("Loan action in AI response not in config");
            knockoutRuleTracking.setMessage("Loan action in AI response not in config");
            knockoutRuleTracking.setLoanActionNotConfig(loanActionNotConfig);
            return null;
        }

        List<String> userProfileRule = new ArrayList<>();
        AIActionMappingConfig currentAIActionMapping = new AIActionMappingConfig();
        if (Identify.CONFIRM.equals(userProfileInfo.getIdentify())) {
            userProfileRule.add(UserProfileConfigValue.IS_KYC_CONFIRM.name());
            String resultMatch = userProfileInfo.getFaceMatching() == FaceMatching.MATCHED ? UserProfileConfigValue.IS_FACE_MATCH.name() : UserProfileConfigValue.FACE_NOT_MATCH.name();
            userProfileRule.add(resultMatch);
            userProfileRule.add(UserProfileConfigValue.ID_CARD_TYPE_UNCHECK.name());

        } else {
            userProfileRule.add(UserProfileConfigValue.KYC_UN_CONFIRM.name());
            userProfileRule.add(UserProfileConfigValue.FACE_MATCHING_UNCHECK.name());
            userProfileRule.add(UserProfileConfigValue.ID_CARD_TYPE_UNCHECK.name());
        }

        setC06VerifiedRule(serviceObInfo, userProfileInfo, userProfileRule);

        currentAIActionMapping.setUserProfileInfos(userProfileRule);
        currentAIActionMapping.setAiLoanActionNames(aiActionNames);

        Log.MAIN.info("User AI action mapping {}", Json.encode(currentAIActionMapping));
        knockoutRuleTracking.setAiActionMappingConfig(currentAIActionMapping);
        for (AiLoanActionConfig aiLoanActionConfig : serviceObInfo.getAiLoanActionConfigs()) {
            if (aiLoanActionConfig.isMapConfig(userProfileRule, aiActionNames)) {
                Log.MAIN.info("AI loan action map success {} - {}", Json.encode(currentAIActionMapping), Json.encode(aiLoanActionConfig));
                knockoutRuleTracking.setMessage("AI loan action map success, result code [" + aiLoanActionConfig.getResultCode() + "]");
                return aiLoanActionConfig;
            }
        }

        if (!isMapBank) {
            Log.MAIN.info("User not map bank");
            knockoutRuleTracking.setMessage("User not map bank");
            AiLoanActionConfig config = new AiLoanActionConfig();
            config.setServiceId(serviceObInfo.getServiceId());
            config.setResultCode(OnboardingErrorCode.NOT_MAP_BANK.getCode());
            config.setRedirectProcessName(OnboardingProcessor.INIT_CONFIRM);
            return config;
        }
        Log.MAIN.fatal("AI RESPONSE ACTION MAPPING NOT EXIST (DROP USER) \nAgentId: [{}] \nPhone: [{}] \nAI Response: [{}]", base.getInitiatorId(), base.getInitiator(), Json.encode(currentAIActionMapping));
        knockoutRuleTracking.setMessage("AI RESPONSE ACTION MAPPING NOT EXIST");
        return null;
    }

    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) {
        return null;
    }

    protected void fillResponseDataWhenReject(R response, OnboardingData<T, R> jobData) {
    }

    protected void setDeviceInfo(ScreenLog startScreenLog, T request) {

    }

    private void setDataMetricKor(OnboardingData<T, R> jobData, LoanDeciderRecord loanDeciderRecord) {
        if (loanDeciderRecord.isEmptyLoanAction()) {
            jobData.updateTagData(Constant.TAG_NAME, NO_ACTION_VALUE);
            return;
        }

        boolean isNFCAction = false;
        boolean isReMapbankAction = false;

        jobData.updateTagData(Constant.TAG_NAME, HIT_ACTION_KOR);

        for (LoanAction aiLoanAction : loanDeciderRecord.getLoanAction()) {
            String actionName = aiLoanAction.getActionName();
            if (actionName.equals(LoanActionType.NFC_IDCARD.name())) {
                isNFCAction = true;
                break;
            } else if (actionName.equals(LoanActionType.RE_MAP_BANK.name())) {
                isReMapbankAction = true;
            }
        }

        if (isNFCAction) {
            jobData.updateTagData(Constant.TAG_NAME, HIT_NFC);
        } else if (isReMapbankAction) {
            jobData.updateTagData(Constant.TAG_NAME, LoanActionType.RE_MAP_BANK.name());
        }
    }

    private String getC06VerifiedKycValue(UserProfileInfo userProfileInfo) {
        if (Utils.isEmpty(userProfileInfo.getC06VerifiedKyc())) {
            return null;
        }

        return String.valueOf(userProfileInfo.getC06VerifiedKyc().getCode());
    }

    private void setC06VerifiedRule(ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, List<String> userProfileRule) {
        String result = userProfileInfo.getC06VerifiedKyc() == KycC06Verified.VERIFIED ? UserProfileConfigValue.C06_VERIFIED.name() : UserProfileConfigValue.C06_UN_VERIFIED.name();
        userProfileRule.add(result);
    }

    private void addTaskData(TaskData taskData, KnockoutRuleTracking knockoutRuleTracking) {
        taskData.setMessage(knockoutRuleTracking.getMessage());
        taskData.setResultCode(knockoutRuleTracking.getResultCode());
    }

}
