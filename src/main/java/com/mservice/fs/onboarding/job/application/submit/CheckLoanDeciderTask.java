package com.mservice.fs.onboarding.job.application.submit;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.http.LoanDeciderService;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.enums.AiUserProfileEnumMap;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.enums.UserGroup;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.LoanActionType;
import com.mservice.fs.onboarding.model.LoanDeciderData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.KycDataAI;
import com.mservice.fs.onboarding.model.application.ScamStatus;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.ScamAlertResult;
import com.mservice.fs.onboarding.model.common.ai.ActionStatus;
import com.mservice.fs.onboarding.model.common.ai.ActionType;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRequest;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.ai.SummaryScreenLog;
import com.mservice.fs.onboarding.model.common.ai.UserActionEvent;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.AiLoanActionConfig;
import com.mservice.fs.onboarding.model.common.config.LoanActionAiConfig;
import com.mservice.fs.onboarding.model.common.config.MiniAppVersionDataService;
import com.mservice.fs.onboarding.model.common.config.PackageInfoConfig;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.Gender;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.template.TemplateMessage;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class CheckLoanDeciderTask<T extends ConfirmRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "CHECK_LOAN_DECIDER";
    private static final String SCREEN_ID = "SUMMARY_SCREEN";
    private final Class<?> responseClass;
    private static final Set<LoanActionType> LOAN_ACTION_TYPE_FACE_MATCHING = Set.of(LoanActionType.FACE_MATCHING, LoanActionType.CHECK_1_N);
    protected static final String NO_ACTION_VALUE = "NO_ACTION";
    protected static final String HIT_ACTION_LOAN_DECIDER = "HIT_ACTION_LOAN_DECIDER";

    public static final String TELCO_ACTION = "VERIFY_OTP_TELCO";

    @Autowire(name = "LoanDecider")
    private LoanDeciderService loanDeciderService;
    @Autowire(name = "PackageDataService")
    private DataService<PackageInfoConfig> packageDataCreator;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire
    private DataService<MiniAppVersionDataService> miniAppVersionDataService;

    @Autowire
    private TemplateMessage templateMessage;

    public CheckLoanDeciderTask() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(getClass(), OnboardingResponse.class);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        T request = jobData.getRequest();
        String serviceId = jobData.getServiceId();
        UserProfileInfo userProfileInfo = jobData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        if (!serviceObInfo.isMatchAction(Action.CHECK_LOAN_DECIDER, jobData.getProcessName())) {
            applicationForm.getApplicationData().setStatus(ApplicationStatus.ACCEPTED_BY_MOMO);
            applicationForm.getApplicationData().setState(ApplicationStatus.ACCEPTED_BY_MOMO.getState());
            applicationForm.getApplicationData().setModifiedDateInMillis(System.currentTimeMillis());
            Log.MAIN.info("Service {} dose not need to check LoanDecider, by pass...", serviceId);
        } else if (serviceObInfo.isMatchAction(Action.CHECK_LOAN_DECIDER, jobData.getProcessName()) && !ScamStatus.VERIFIED.name().equals(applicationForm.getApplicationData().getScamStatus())) {
            String partnerRequest = createLoanDeciderRequest(userProfileInfo, jobData, request, serviceObInfo, applicationForm);
            LoanDeciderResponse loanDeciderResponse = loanDeciderService.callApi(partnerRequest, jobData.getBase(), serviceObInfo.getAiConfig());
            validateLoanDeciderResponse(loanDeciderResponse, jobData, serviceObInfo, userProfileInfo, applicationForm);
            taskData.setContent(loanDeciderResponse);
        }
        finish(jobData, taskData);
    }

    private String createLoanDeciderRequest(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, T request, ServiceObInfo serviceObInfo, ApplicationForm applicationForm) throws BaseException, ValidatorException, Exception {
        String phoneNumber = jobData.getInitiator();
        List<SummaryScreenLog.RelativeInfo> relativeInfos = new ArrayList<>();
        ApplicationData applicationData = applicationForm.getApplicationData();
        Optional.ofNullable(applicationData.getReferencePeople()).ifPresent(referencePeoples -> referencePeoples.forEach(
                referencePeople -> {
                    SummaryScreenLog.RelativeInfo relativeInfo = new SummaryScreenLog.RelativeInfo();
                    relativeInfo.setRelativeName(referencePeople.getFullName());
                    relativeInfo.setRelativePhoneNumber(referencePeople.getPhoneNumber());
                    relativeInfo.setRelationship(referencePeople.getRelationship().getName());
                    relativeInfos.add(relativeInfo);
                }
        ));
        SummaryScreenLog.Profile profile = new SummaryScreenLog.Profile();
        profile.setUserName(userProfileInfo.getFullNameKyc());
        profile.setUserEmail(applicationData.getEmail());
        profile.setAddress(applicationData.getPermanentAddress().getFullAddress());
        profile.setUserDob(userProfileInfo.getDobKyc());
        profile.setIssueDate(userProfileInfo.getIssueDateKyc());
        profile.setNationalIdNumber(userProfileInfo.getPersonalIdKyc());
        if (Utils.isEmpty(userProfileInfo.getIdCardTypeDetailKyc())) {
            profile.setIdCardType(SummaryScreenLog.IdCardType.UNKNOWN_ID_CARD_TYPE.getCode());
        } else {
            SummaryScreenLog.IdCardType aiIdCardType = AiUserProfileEnumMap.idCardTypeMap.get(userProfileInfo.getIdCardTypeDetailKyc());
            if (aiIdCardType != null) {
                profile.setIdCardType(aiIdCardType.getCode());
            } else {
                profile.setIdCardType(SummaryScreenLog.IdCardType.OTHER_TYPE.getCode());
            }
        }

        Gender gender = userProfileInfo.getGenderKyc();
        if (Utils.isNotEmpty(gender)) {
            profile.setGender(AiUserProfileEnumMap.genderMap.get(gender).getCode());
        }
        profile.setUserIncome(Utils.isEmpty(applicationData.getIncome()) ? 0L : applicationData.getIncome());
        profile.setExpireDate(userProfileInfo.getExpiredDateKyc());
        profile.setRelativesInfo(relativeInfos);
        profile.setPhoneNumber(phoneNumber);
        profile.setTaxCode(applicationData.getTaxId());
        if (Utils.isNotEmpty(AiUserProfileEnumMap.kycStatusMap.get(userProfileInfo.getIdentify()))) {
            profile.setKycConfirmStatus(AiUserProfileEnumMap.kycStatusMap.get(userProfileInfo.getIdentify()).name());
        }

        if (Utils.isNotEmpty(AiUserProfileEnumMap.faceStatusMap.get(userProfileInfo.getFaceMatching()))) {
            profile.setFaceMatchingStatus(AiUserProfileEnumMap.faceStatusMap.get(userProfileInfo.getFaceMatching()).name());
        }
        profile.setFrontImagePath(userProfileInfo.getIdFrontImageKyc());
        profile.setBackImagePath(userProfileInfo.getIdBackImageKyc());
        try {
            Long createTime = Long.parseLong(userProfileInfo.getCreateDate());
            profile.setWalletCreatedTime(createTime);
        } catch (NumberFormatException e) {
            // skip
            Log.MAIN.info("Parse number error: [{}]", userProfileInfo.getCreateDate());
        }
        profile.setSubBankCode(userProfileInfo.getSubBankCode());
        profile.setKycC06Verified(getC06VerifiedKycValue(userProfileInfo));

        //merchant
        profile.setMerchantName(userProfileInfo.getMerchantName());
        profile.setMerchantAutoCashout(userProfileInfo.isMerchantAutoCashout());
        profile.setM4bFlag(userProfileInfo.getM4bFlag());


        buildUserActionEventAlertScam(jobData, request, profile);

        SummaryScreenLog.CashLoanItem cashLoanItem = new SummaryScreenLog.CashLoanItem();
        PackageInfo packageInfo = applicationData.getChosenPackage();

        cashLoanItem.setLoanAmount(packageInfo.getLoanAmount());
        SummaryScreenLog.PaylaterLoanItem paylaterLoanItem = new SummaryScreenLog.PaylaterLoanItem();
        paylaterLoanItem.setLoanAmount(packageInfo.getLoanAmount());

        KycDataAI kycDataAI = new KycDataAI();
        kycDataAI.setFace_selfie_image_s3_path(userProfileInfo.getImageFaceMatching());

        AiConfig aiConfig = serviceObInfo.getAiConfig();
        String lenderId = packageInfo.getLenderId();
        SummaryScreenLog summaryScreenLog = new SummaryScreenLog();
        summaryScreenLog.setAgentId(jobData.getInitiatorId());
        summaryScreenLog.setScreenId(SCREEN_ID);
        summaryScreenLog.setSelectedLoanPackage(packageInfo.getPackageCode());
        summaryScreenLog.setMomoCreditScore(applicationData.getCurrentCreditScore());
        summaryScreenLog.setCashLoanItem(cashLoanItem);
        summaryScreenLog.setPaylaterLoanItem(paylaterLoanItem);
        summaryScreenLog.setTimestamp(System.currentTimeMillis());
        summaryScreenLog.setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()).toLowerCase());
        summaryScreenLog.setLenderId(lenderId);
        summaryScreenLog.setProductId(aiConfig.getProductId());
        summaryScreenLog.setMiniAppVersion((String) jobData.getHeader(Constant.MINI_APP_TRACK_VER));
        summaryScreenLog.setScreenOrder(aiConfig.getScreenOrder());
        summaryScreenLog.setProfile(profile);
        summaryScreenLog.setKycData(kycDataAI);
        summaryScreenLog.setUserGroup(getGroup(userProfileInfo));

        LoanDeciderRequest loanDeciderRequest = new LoanDeciderRequest();
        loanDeciderRequest.setAppSessionId(Utils.isEmpty(jobData.getBase().getHeaders().get(Constant.MOMO_SESSION_KEY)) ? CommonConstant.STRING_EMPTY : jobData.getBase().getHeaders().get(Constant.MOMO_SESSION_KEY).toString());
        loanDeciderRequest.setRequestId(request.getRequestId() + "_" + jobData.getTraceId());
        loanDeciderRequest.setRequestTimestamp(System.currentTimeMillis());
        loanDeciderRequest.setProductGroup(aiConfig.getProductGroup());
        loanDeciderRequest.setSourceId(aiConfig.getSourceId());
        loanDeciderRequest.setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()).toLowerCase());
        loanDeciderRequest.setProductId(aiConfig.getProductId());
        loanDeciderRequest.setAgentId(Long.parseLong(jobData.getInitiatorId()));
//        loanDeciderRequest.setMerchantId(Utils.nullToEmpty(request.getTargetAgent())); todo where to get targetAgent
        loanDeciderRequest.setSummaryScreenLog(summaryScreenLog);
        loanDeciderRequest.setMomoLoanAppId(applicationData.getApplicationId());
        loanDeciderRequest.setMessageType(AIMessageType.SUMMARY_SCREEN_LOG.name());
        loanDeciderRequest.setScreenOrder(1);
        loanDeciderRequest.setLenderId(lenderId);
        loanDeciderRequest.setSegmentUser(Utils.nullToEmpty(packageInfo.getSegmentUser()));
        loanDeciderRequest.setExperimentTag(Utils.nullToEmpty(packageInfo.getExperimentTag()));
        return loanDeciderRequest.encode();
    }

    private void buildUserActionEventAlertScam(OnboardingData<T, R> jobData, T request, SummaryScreenLog.Profile profile) throws BaseException, ValidatorException, Exception {
        ScamAlertResult scamAlertResult = request.getScamAlertResult();
        long now = System.currentTimeMillis();

        if (Utils.isNotEmpty(scamAlertResult)) {
            UserActionEvent event = OnboardingUtils.buildUserActionEvent(scamAlertResult);
            profile.setUserActionEvent(List.of(event));
        } else if (isCacheUserActionEvent(jobData)) {
            CacheData cacheData = jobData.getTaskData(GetCacheTask.NAME).getContent();
            ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
            profile.setUserActionEvent(List.of(applicationListWrapper.getUserActionEvent()));
        } else if (miniAppVersionDataService.getData().isMiniAppVersionNotSupportAction(jobData, LoanActionType.SHOW_SCAM_ALERT_TEXT.name())) {

            UserActionEvent event = new UserActionEvent();
            event.setActionType(ActionType.UNKNOWN_ACTION_TYPE.getType());
            event.setActionId(ActionType.UNKNOWN_ACTION_TYPE.getId());
            event.setEndTimestamp(now);
            event.setStartTimestamp(now);
            event.setActionStatus(ActionStatus.UNAVAILABLE.name());
            profile.setUserActionEvent(List.of(event));
        }
    }

    private boolean isCacheUserActionEvent(OnboardingData<T, R> jobData) {
        CacheData cacheData = jobData.getTaskData(GetCacheTask.NAME).getContent();
        if (Utils.isEmpty(cacheData)) {
            return false;
        }
        ApplicationListWrapper applicationListWrapper = (ApplicationListWrapper) cacheData.getObject();
        if (Utils.isEmpty(applicationListWrapper)) {
            return false;
        }
        return Utils.isNotEmpty(applicationListWrapper.getUserActionEvent());
    }

    private String getGroup(UserProfileInfo userProfileInfo) {
        if (Utils.isEmpty(userProfileInfo.getPersonalIdKyc())
                || Utils.isEmpty(userProfileInfo.getDobKyc())
                || Utils.isEmpty(userProfileInfo.getFullNameKyc())
                || Utils.isEmpty(userProfileInfo.getIssueDateKyc())
                || Utils.isEmpty(userProfileInfo.getExpiredDateKyc())
                || Utils.isEmpty(userProfileInfo.getAddressKyc())
                || Utils.isEmpty(userProfileInfo.getPathFrontImage())
                || Utils.isEmpty(userProfileInfo.getPathBackImage())
        ) {
            return UserGroup.GROUP_B.getCode();
        }
        return UserGroup.GROUP_A.getCode();
    }

    private void validateLoanDeciderResponse(LoanDeciderResponse loanDeciderResponse, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, ApplicationForm applicationForm) throws BaseException, Exception, ValidatorException {
        if (Utils.isEmpty(loanDeciderResponse.getLoanDeciderRecord())) {
            Log.MAIN.error("Call Knock out rule, loanDeciderRecord is null");
            throw new BaseException(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
        }

        AIStatus loanDeciderStatus = loanDeciderResponse.getLoanDeciderRecord().getLoanDeciderStatus();
        R response = (R) Generics.createObject(responseClass);
        ApplicationData applicationData = applicationForm.getApplicationData();
        setProfileFromAIToApplicationData(loanDeciderResponse, applicationData);
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(jobData.getPartnerId());
        updateScoreApplication(loanDeciderResponse, applicationForm.getApplicationData(), partnerConfig);

        switch (loanDeciderStatus) {
            case REJECT:
                applicationData.setStatus(ApplicationStatus.REJECTED_BY_LOAN_DECIDER);
                applicationData.setState(ApplicationStatus.REJECTED_BY_LOAN_DECIDER.getState());
                applicationData.setModifiedDateInMillis(System.currentTimeMillis());
                response.setResultCode(OnboardingErrorCode.LOAN_DECIDER_REJECT);
                jobData.setResponse(response);
                break;
            case APPROVE:
                //CCM have to check Loan action when call FM
                //CLO does not call Loan action
                if (!serviceObInfo.isMatchAction(Action.CHECK_LOAN_DECIDER, jobData.getProcessName())) {
                    Log.MAIN.info("Service does not need to check loan decider... pass");
                    return;
                }
                setDataMetric(jobData, loanDeciderResponse);
                boolean isActionTelco = isActionOtpTelco(jobData, loanDeciderResponse);
                if (isActionTelco) {
                    Log.MAIN.info("MATCH ACTION TELCO");
                    break;
                }

                if (skipActionNotSupport(jobData, loanDeciderResponse)) {
                    Log.MAIN.info("LoanAction : {}", loanDeciderResponse);
                }

                if (!serviceObInfo.isAiActionMappingLD()) {
                    processSingleActionDecider(jobData, loanDeciderResponse, applicationData, serviceObInfo, applicationForm, userProfileInfo, response);
                } else {
                    processMappingActionDecider(jobData, loanDeciderResponse, applicationData, serviceObInfo, applicationForm, userProfileInfo, response);
                }
                break;

            default:
                Log.MAIN.error("Loan decider with invalid status {}", loanDeciderStatus);
                throw new BaseException(OnboardingErrorCode.CALL_AI_ERROR);
        }
    }

    protected boolean isActionOtpTelco(OnboardingData<T, R> jobData, LoanDeciderResponse loanDeciderResponse) throws BaseException, ValidatorException, Exception {
        return false;
    }

    protected boolean skipActionNotSupport(OnboardingData<T, R> jobData, LoanDeciderResponse loanDeciderResponse) {
        LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return false;
        }
        int sizeBefore = loanActions.size();
        loanActions.removeIf(action -> {
            try {
                return miniAppVersionDataService.getData().isMiniAppVersionNotSupportAction(jobData, action.getActionName());
            } catch (Exception | BaseException | ValidatorException e) {
                return false;
            }
        });
        return sizeBefore != loanActions.size();
    }

    private void setDataMetric(OnboardingData<T, R> jobData, LoanDeciderResponse loanDeciderResponse) {
        try {
            LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
            List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
            if (Utils.isEmpty(loanActions)) {
                jobData.updateTagData(Constant.LOAN_DECIDER_TAG_NAME, NO_ACTION_VALUE);
            } else {
                jobData.updateTagData(Constant.LOAN_DECIDER_TAG_NAME, HIT_ACTION_LOAN_DECIDER);
            }
        } catch (Exception e) {
            Log.MAIN.error("Error when setDataMetric LoanDecider", e);
        }

    }

    protected boolean verifyFaceMatchingFromApp(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, UserProfileInfo userProfileInfo) {
        return false;
    }

    private void setLoanActionFaceMatchingToApplicationData(LoanActionAiConfig loanActionAiConfig, ApplicationData applicationData) {
        if (Utils.isEmpty(loanActionAiConfig)) {
            Log.MAIN.info("LoanActionAiConfig is empty not set facematching");
            return;
        }
        String actionName = loanActionAiConfig.getActionName();
        if (Utils.isEmpty(loanActionAiConfig)) {
            Log.MAIN.info("ActionName: {} is empty not set facematching", actionName);
            return;
        }
        LoanActionType loanActionType = LoanActionType.valueOf(actionName);
        if (LOAN_ACTION_TYPE_FACE_MATCHING.contains(loanActionType)) {
            Log.MAIN.info("ActionName: {} in List {} => set to cache", actionName, LOAN_ACTION_TYPE_FACE_MATCHING);
            applicationData.getLoanDeciderData().setRequiredActionLoanDecider(loanActionType);
        } else {
            Log.MAIN.info("ActionName: {} NOT in List {} => not set to cache", actionName, LOAN_ACTION_TYPE_FACE_MATCHING);
        }
    }

    private void processSingleActionDecider(OnboardingData<T, R> jobData, LoanDeciderResponse loanDeciderResponse, ApplicationData applicationData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, UserProfileInfo userProfileInfo, R response) throws Exception {
        LoanAction loanAction = LoanDeciderRecord.findFirstLoanAction(loanDeciderResponse.getLoanDeciderRecord());
        if (loanAction == null || verifyFaceMatchingFromApp(jobData, serviceObInfo, applicationForm, userProfileInfo)) {
            Log.MAIN.info("Do not check loan action ");
            applicationData.setStatus(ApplicationStatus.ACCEPTED_BY_MOMO);
            applicationData.setState(ApplicationStatus.ACCEPTED_BY_MOMO.getState());
            applicationData.setModifiedDateInMillis(System.currentTimeMillis());
            return;
        }
        if (LoanActionType.PENDING_APPLICATION.name().equals(loanAction.getActionName())) {
            applicationData.setScamStatus(ScamStatus.PENDING.name());
        }
        Log.MAIN.info("Check Loan Action {}", loanAction);
        LoanActionAiConfig loanActionAiConfig = serviceObInfo.getLoanDeciderConfigMap().get(loanAction.getActionName());
        response = OnboardingUtils.createAiRuleResponse(responseClass, loanActionAiConfig, jobData, applicationForm);
        setResultCodeByServiceLoanAction(response, loanActionAiConfig, serviceObInfo, userProfileInfo);
        setLoanActionFaceMatchingToApplicationData(loanActionAiConfig, applicationData);
        jobData.setResponse(response);
    }

    private void processMappingActionDecider(OnboardingData<T, R> jobData, LoanDeciderResponse loanDeciderResponse, ApplicationData applicationData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, UserProfileInfo userProfileInfo, R response) {
        LoanDeciderRecord loanDeciderRecord = loanDeciderResponse.getLoanDeciderRecord();
        List<LoanActionType> requiredActionLoanDeciderList = new ArrayList<>();
        applicationData.getLoanDeciderData().setRequiredActionLoanDeciderList(requiredActionLoanDeciderList);

        if (loanDeciderRecord.getLoanAction().isEmpty() || verifyFaceMatchingFromApp(jobData, serviceObInfo, applicationForm, userProfileInfo)) {
            Log.MAIN.info("Do not check loan action ");
            applicationData.setStatus(ApplicationStatus.ACCEPTED_BY_MOMO);
            applicationData.setState(ApplicationStatus.ACCEPTED_BY_MOMO.getState());
            applicationData.setModifiedDateInMillis(System.currentTimeMillis());
            return;
        }

        for (LoanAction aiLoanAction : loanDeciderRecord.getLoanAction()) {
            LoanActionType loanActionType = LoanActionType.valueOf(aiLoanAction.getActionName());
            requiredActionLoanDeciderList.add(loanActionType);
            if (LoanActionType.PENDING_APPLICATION.equals(loanActionType)) {
                applicationData.setScamStatus(ScamStatus.PENDING.name());
            }
        }

        AiLoanActionConfig config = OnboardingUtils.mappingLoanDeciderAction(serviceObInfo, requiredActionLoanDeciderList, userProfileInfo);
        if (config == null) {
            response.setResultCode(OnboardingErrorCode.RESPONSE_FROM_AI_ERROR);
            jobData.setResponse(response);
            return;
        }

        jobData.putProcessNameToTemPlateModel(config.getRedirectProcessName());
        response.setResultCode(config.getResultCode());
        jobData.setResponse(response);
    }

    private void setResultCodeByServiceLoanAction(R response, LoanActionAiConfig loanActionAiConfig, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo) {
        Log.MAIN.info("Check set ResultCode with loanAction {}", loanActionAiConfig);

        if (Utils.isEmpty(loanActionAiConfig)) {
            Log.MAIN.info("LoanActionConfig is empty not set resultCodeByServiceLoanAction");
            return;
        }

        Integer loanActionResultCode = serviceObInfo.getActionType().getResultCodeWithActionLoanDecider(loanActionAiConfig.getActionName(), userProfileInfo);
        if (Utils.isNotEmpty(loanActionResultCode)) {
            Log.MAIN.info("Set resultCode response with ResultCode of loan action is : {}", loanActionResultCode);
            response.setResultCode(loanActionResultCode);
        }
    }

    private void setProfileFromAIToApplicationData(LoanDeciderResponse loanDeciderResponse, ApplicationData applicationData) throws Exception {

        LoanDeciderData loanDeciderData = applicationData.getLoanDeciderData();
        if (Utils.isEmpty(loanDeciderData)) {
            loanDeciderData = new LoanDeciderData();
        }
        loanDeciderData.setProfile(loanDeciderResponse.getLoanDeciderRecord().getProfile());
        loanDeciderData.setMerchantProfile(loanDeciderResponse.getLoanDeciderRecord().getMerchantProfile());
        loanDeciderData.setExperimentTag(loanDeciderResponse.getLoanDeciderRecord().getExperimentTag());
        applicationData.setLoanDeciderData(loanDeciderData);
    }

    private void updateScoreApplication(LoanDeciderResponse loanDeciderResponse, ApplicationData applicationData, PartnerConfig partnerConfig) {
        if (partnerConfig.isApplyScoreAtLoanDecider() &&
                Utils.isNotEmpty(loanDeciderResponse) &&
                Utils.isNotEmpty(loanDeciderResponse.getLoanDeciderRecord()) &&
                Utils.isNotEmpty(loanDeciderResponse.getLoanDeciderRecord().getMomoCreditScore())) {
            applicationData.setCurrentCreditScore(loanDeciderResponse.getLoanDeciderRecord().getMomoCreditScore());
        }
    }

    private String getC06VerifiedKycValue(UserProfileInfo userProfileInfo) {
        if (Utils.isEmpty(userProfileInfo.getC06VerifiedKyc())) {
            return null;
        }

        return String.valueOf(userProfileInfo.getC06VerifiedKyc().getCode());
    }
}
