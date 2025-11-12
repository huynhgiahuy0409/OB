package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.http.PackageInfoService;
import com.mservice.fs.onboarding.enums.AIErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.OfferPackage;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.application.UserProfileAI;
import com.mservice.fs.onboarding.model.common.ai.DeviceInfoAI;
import com.mservice.fs.onboarding.model.common.ai.GetPackageRequest;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.config.*;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.*;

/**
 * @author hoang.thai on 8/28/2023
 */
public abstract class PackageAiTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_PACKAGE-AI";

    @Autowire(name = "GetPackage")
    private PackageInfoService packageInfoService;
    @Autowire(name = "PackageDataService")
    private DataService<PackageInfoConfig> packageDataCreator;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    private final Class<?> responseClass;
    private static final int LIST_PACKAGE_EMPTY_METRIC_CODE = 6000;

    public PackageAiTask() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(this.getClass(), OnboardingResponse.class);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        UserProfileInfo userProfileInfo = getUserProfileInfo(jobData);
        ServiceObInfo serviceObInfo = getServiceObInfo(jobData, onboardingDataInfo);
        if (validate(jobData, serviceObInfo) && serviceObInfo.isMatchAction(Action.GET_PACKAGE, jobData.getProcessName())) {
            PackageCache packageCache = createPackageCache(jobData, serviceObInfo, userProfileInfo, taskData);
            taskData.setContent(packageCache);
        }
        finish(jobData, taskData);
    }

    protected UserProfileInfo getUserProfileInfo(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
    }

    protected boolean validate(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo) {
        return true;
    }

    private String createRequestGetPackageAi(OnboardingData<T, R> jobData, UserProfileInfo userProfileInfo, AiConfig aiConfig) throws BaseException {
        try {
            Base base = jobData.getBase();
            T request = jobData.getRequest();
            String loanProductCode = Utils.nullToEmpty(aiConfig.getLoanProductCode());
            GetPackageRequest getPackageRequest = new GetPackageRequest();
            getPackageRequest.setAppSessionId(Utils.isEmpty(base.getHeaders().get(Constant.MOMO_SESSION_KEY)) ? CommonConstant.STRING_EMPTY : base.getHeaders().get(Constant.MOMO_SESSION_KEY).toString());
            getPackageRequest.setRequestId(request.getRequestId() + "_" + jobData.getTraceId());
            getPackageRequest.setRequestTimestamp(String.valueOf(System.currentTimeMillis()));
            getPackageRequest.setProductGroup(aiConfig.getProductGroup());
            getPackageRequest.setLoanProductCode(loanProductCode.toUpperCase());
            getPackageRequest.setProductId(aiConfig.getProductId());
            getPackageRequest.setAgentId(Integer.parseInt(jobData.getInitiatorId()));
            getPackageRequest.setMerchantId(aiConfig.getMerchantId());
            getPackageRequest.setLenderId(getLenderId(request));
            getPackageRequest.setSegmentUser(getSegmentUser(jobData));
            getPackageRequest.setNationalId(userProfileInfo.getPersonalIdKyc());
            getPackageRequest.setSourceId(aiConfig.getSourceIdPackageFromBase(base));
            UserProfileAI userProfileAI = new UserProfileAI();
            userProfileAI.setUserDob(OnboardingUtils.formatDateAI(userProfileInfo.getDobKyc()));
            userProfileAI.setUserEmail(userProfileInfo.getEmail());
            userProfileAI.setUserName(userProfileInfo.getFullNameKyc());
            userProfileAI.setPhoneNumber(base.getInitiator());
            try {
                Long createTime = Long.parseLong(userProfileInfo.getCreateDate());
                userProfileAI.setWalletCreatedTime(createTime);
            } catch (NumberFormatException e) {
                // skip
                Log.MAIN.info("Parse number error: [{}]", userProfileInfo.getCreateDate());
            }
            //merchant
            userProfileAI.setMerchantName(userProfileInfo.getMerchantName());
            userProfileAI.setMerchantAutoCashout(userProfileInfo.isMerchantAutoCashout());
            userProfileAI.setM4bFlag(userProfileInfo.getM4bFlag());

            getPackageRequest.setUserProfile(userProfileAI);
            //Set deviceInfo
            DeviceInfoAI deviceInfoAI = new DeviceInfoAI();
            deviceInfoAI.setDeviceOS(request.getDeviceOS());
            getPackageRequest.setDeviceInfo(deviceInfoAI);

            return JsonUtil.toString(getPackageRequest);
        } catch (Exception ex) {
            Log.MAIN.error("Error when create Request get package", ex);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
    }

    protected String getLenderId(T request) {
        return Constant.ALL_LENDER;
    }

    protected abstract String getSegmentUser(OnboardingData<T, R> jobData);


    protected PackageCache createPackageCache(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, TaskData taskData) throws BaseException, Exception, ValidatorException {
        AiConfig aiConfig = serviceObInfo.getAiConfig();
        String partnerRequest = createRequestGetPackageAi(jobData, userProfileInfo, aiConfig);
        GetPackageResponse packageAiResponse = packageInfoService.callApi(partnerRequest, jobData.getBase());

        Integer responseAiCode = packageAiResponse.getResponseCode();
        switch (responseAiCode) {
            case AIErrorCode.Code.INTERNAL_SERVER_ERROR:
            case AIErrorCode.Code.SERVICE_UNAVAILABLE:
                addMetric(taskData, responseAiCode, null, null, jobData);
                throw new BaseException(OnboardingErrorCode.CALL_AI_ERROR);
            case AIErrorCode.Code.BAD_REQUEST: {
                Log.MAIN.info("Bad Request: User not found package");
                addMetric(taskData, responseAiCode, null, null, jobData);
                processWithBadRequest(packageAiResponse, userProfileInfo, jobData, serviceObInfo);
                return null;
            }
            case AIErrorCode.Code.SUCCESS: {
                //CLO and another service do not check rekyc will return onboarding error code null
                return processPackage200(jobData, serviceObInfo, userProfileInfo, taskData, packageAiResponse, responseAiCode);
            }
            case AIErrorCode.Code.QUICK:
                if (aiConfig.isQuickPackage(jobData.getBase())) {
                    return processPackageQuick(jobData, serviceObInfo, userProfileInfo, taskData, packageAiResponse, responseAiCode);
                }
                Log.MAIN.error("Response quick for api normal {} - {}", jobData.getProcessName(), responseAiCode);
                throw new BaseException(CommonErrorCode.SYSTEM_BUG);
            default:
                Log.MAIN.error("Unsupported response code from AI: {}", responseAiCode);
                throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
    }

    protected PackageCache processPackageQuick(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, TaskData taskData, GetPackageResponse packageAiResponse, Integer responseAiCode) throws BaseException, ValidatorException, Exception {
        processWhenSuccess(serviceObInfo, packageAiResponse, userProfileInfo, jobData);
        PackageCache packageCache = buildPackageCacheSuccess(packageAiResponse, serviceObInfo);
        Map<String, PackageInfo> packageInfoMap = Utils.isEmpty(packageCache) || Utils.isEmpty(packageCache.getPackageInfoMap()) ? new HashMap<>() : packageCache.getPackageInfoMap();
        List<OfferPackage> offerPackagesNotConfiguredInDb = Utils.isEmpty(packageCache) || Utils.isEmpty(packageCache.getOfferPackagesNotConfiguredInDb()) ? new ArrayList<>() : packageCache.getOfferPackagesNotConfiguredInDb();
        addMetric(taskData, responseAiCode, packageInfoMap, offerPackagesNotConfiguredInDb, jobData);
        return packageCache;
    }


    protected PackageCache processPackage200(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, UserProfileInfo userProfileInfo, TaskData taskData, GetPackageResponse packageAiResponse, Integer responseAiCode) throws BaseException, ValidatorException, Exception {
        processWhenSuccess(serviceObInfo, packageAiResponse, userProfileInfo, jobData);
        PackageCache packageCache = buildPackageCacheSuccess(packageAiResponse, serviceObInfo);
        Map<String, PackageInfo> packageInfoMap = Utils.isEmpty(packageCache) || Utils.isEmpty(packageCache.getPackageInfoMap()) ? new HashMap<>() : packageCache.getPackageInfoMap();
        List<OfferPackage> offerPackagesNotConfiguredInDb = Utils.isEmpty(packageCache) || Utils.isEmpty(packageCache.getOfferPackagesNotConfiguredInDb()) ? new ArrayList<>() : packageCache.getOfferPackagesNotConfiguredInDb();
        addMetric(taskData, responseAiCode, packageInfoMap, offerPackagesNotConfiguredInDb, jobData);
        return packageCache;
    }

    private PackageCache buildPackageCacheSuccess(GetPackageResponse packageAiResponse, ServiceObInfo serviceObInfo) throws BaseException, ValidatorException, Exception {
        List<OfferPackage> offerPackages = packageAiResponse.getOfferPackages();
        LinkedHashMap<String, PackageInfo> packageInfoMap = new LinkedHashMap<>();
        List<OfferPackage> offerPackagesNotConfiguredInDb = new ArrayList<>();
        PackageCache packageCache = new PackageCache();
        packageCache.setMomoCreditScore(packageAiResponse.getMomoCreditScore());
        packageCache.setGetPackageResponse(packageAiResponse);
        if (offerPackages == null) {
            return packageCache;
        }
        String lenderLogic = packageAiResponse.getLenderLogic();
        for (OfferPackage offerPackage : offerPackages) {
            PackageInfo packageInfo = packageDataCreator.getData().getPackage(offerPackage.getPackageCode());
            if (packageInfo == null) {
                Log.MAIN.info("Package with code '{}' not found in the database.", offerPackage.getPackageCode());
                offerPackagesNotConfiguredInDb.add(offerPackage);
                if (Utils.isEmpty(getConfig().getLendingServiceProductCodeMap().get(serviceObInfo.getServiceId()))) {
                    Log.MAIN.error("Lending denied: service '{}' is not whitelisted.", serviceObInfo.getServiceId());
                    throw new BaseException(CommonErrorCode.SYSTEM_BUG);
                }
                continue;
            }
            packageInfo.setLenderLogic(lenderLogic);
            packageInfo.setRank(offerPackage.getRank());
            packageInfo.setPackageStatus(offerPackage.getPackageStatus());
            packageInfo.setSegmentUser(packageAiResponse.getSegmentUser());
            packageInfo.setExperimentTag(packageAiResponse.getExperimentTag());
            OnboardingUtils.setInterestRate(getConfig().getWhiteListPackageInterestRate(), packageInfo);

            if (serviceObInfo.isDoCalculateDueDate()) {
                packageInfo.setDueDay(OnboardingUtils.getDueDate(serviceObInfo, offerPackage.getLenderId()));
            }
            if (Utils.isNotEmpty(packageInfo.getEmi())) {
                packageInfo.setDailyEquivalentAmount(OnboardingUtils.computeDailyEquivalentAmount(packageInfo.getEmi()));
            }
            if (Utils.isNotEmpty(packageInfo.getEmi())) {
                packageInfo.setDailyEquivalentAmount(OnboardingUtils.computeDailyEquivalentAmount(packageInfo.getEmi()));
            }
            serviceObInfo.getActionType().handleWhenAddPackageMap(packageInfoMap, packageInfo);
        }
        Log.MAIN.info("PackageInfos: {}", packageInfoMap);
        packageCache.setPackageInfoMap(packageInfoMap);
        Log.MAIN.info("Offer packages not configured in the database: {}", offerPackagesNotConfiguredInDb);
        packageCache.setOfferPackagesNotConfiguredInDb(offerPackagesNotConfiguredInDb);
        return packageCache;
    }

    private void addMetric(TaskData taskData, Integer responseAiCode, Map<String, PackageInfo> packageInfoMap, List<OfferPackage> offerPackagesNotConfiguredInDb, OnboardingData<T, R> jobData) {
        if (AIErrorCode.Code.SUCCESS == responseAiCode) {
            if (OnboardingUtils.isPackageEmpty(packageInfoMap) && Utils.isEmpty(offerPackagesNotConfiguredInDb)) {
                Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {} with metric code {}", responseAiCode, packageInfoMap, LIST_PACKAGE_EMPTY_METRIC_CODE);
                taskData.setResultCode(LIST_PACKAGE_EMPTY_METRIC_CODE);
            } else if (Utils.isEmpty(offerPackagesNotConfiguredInDb)) {
                String lenderId = packageInfoMap.values().iterator().next().getLenderId();
                Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {} with metric code {}", responseAiCode, packageInfoMap, lenderId);
                jobData.updateTagData(Constant.PACKAGE_TAG_NAME, lenderId);
                Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {}", responseAiCode, packageInfoMap);
                taskData.setResultCode(responseAiCode);
            }
        } else {
            Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {}", responseAiCode, packageInfoMap);
            taskData.setResultCode(responseAiCode);
        }
    }

    protected void processWhenSuccess(ServiceObInfo serviceObInfo, GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData) throws BaseException {
    }

    protected void processWithBadRequest(GetPackageResponse packageAiResponse, UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo) throws BaseException {
        throw new BaseException(OnboardingErrorCode.CALL_AI_ERROR);
    }

    @Override
    protected boolean isAsync() {
        return false;
    }

    protected ServiceObInfo getServiceObInfo(OnboardingData<T, R> jobData, DataService<ServiceObConfig> onboardingDataInfo) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
    }

}
