package com.mservice.fs.onboarding.job.application.init.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.enums.PrefixOnboarding;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.ChosenPackage;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.model.application.init.InitApplicationDataInfo;
import com.mservice.fs.onboarding.model.common.config.PackageInfoConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
public class HandlePendingFormTask extends OnboardingTask<InitFormRequest, InitFormResponse> {

    public static final TaskName NAME = () -> "HANDLE_PENDING_FORM";

    @Autowire(name = "PackageDataService")
    private DataService<PackageInfoConfig> packageDataService;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;


    public HandlePendingFormTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<InitFormRequest, InitFormResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        InitFormRequest request = onboardingData.getRequest();
        String partnerId = request.getPartnerId();
        String serviceId = onboardingData.getServiceId();

        InitApplicationDataInfo initApplicationDataInfo = onboardingData.getTaskData(CacheTask.NAME).getContent();
        CacheData packageCacheData = initApplicationDataInfo.getCachePackage();

        ChosenPackage chosenPackage = request.getChosenPackage();

        PackageCache packageCache = null;
        PackageInfo packageInfo = null;
        if (chosenPackage != null) {
            OnboardingErrorCode onboardingErrorCode = getErrorCodeWhenValidatePackage(packageCacheData);
            if (Utils.isNotEmpty(onboardingErrorCode)) {
                InitFormResponse response = new InitFormResponse();
                response.setResultCode(onboardingErrorCode);
                onboardingData.setResponse(response);
                finish(onboardingData, taskData);
                return;
            }
            packageCache = (PackageCache) packageCacheData.getObject();
            packageInfo = getPackageInfo(chosenPackage, packageCache);
        }

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        String applicationId = generateApplicationId(onboardingData, serviceObInfo, packageInfo);
        Log.MAIN.info("Generate new contractId: agentId {} - serviceId {} - applicationId - {}", onboardingData.getInitiatorId(), onboardingData.getServiceId(), applicationId);
        onboardingData.putDataToTemPlateModel(Constant.APPLICATION_ID_KEY, applicationId);
        ApplicationForm applicationForm = createApplicationCache(applicationId, partnerId, onboardingData, packageInfo, packageCache, serviceObInfo);
        Log.MAIN.info("ApplicationCache {}", JsonUtil.toString(applicationForm));

        onboardingData.getTemplateModel().put(Constant.APPLICATION_DATA, applicationForm.getApplicationData());
        taskData.setContent(applicationForm);

        finish(onboardingData, taskData);

    }

    private OnboardingErrorCode getErrorCodeWhenValidatePackage(CacheData packageCacheData) throws JsonProcessingException {
        if (packageCacheData == null) {
            Log.MAIN.info("Package not found from cache");
            return OnboardingErrorCode.PACKAGE_NOT_FOUND_FROM_CACHE;
        }
        PackageCache packageCache = (PackageCache) packageCacheData.getObject();
        if (packageCache == null) {
            Log.MAIN.info("PackageObject is null or empty");
            return OnboardingErrorCode.PACKAGE_NOT_FOUND_FROM_CACHE;
        }
        LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
        if (Utils.isEmpty(packageInfoMap)) {
            Log.MAIN.info("packageInfoMap is null or empty");
            return OnboardingErrorCode.PACKAGE_NOT_FOUND_FROM_CACHE;
        }
        return null;
    }

    private PackageInfo getPackageInfo(ChosenPackage chosenPackage, PackageCache packageCache) throws BaseException, Exception {
        LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
        PackageInfo packageInfo = packageInfoMap.get(chosenPackage.getPackageCode());
        if (packageInfo == null) {
            Log.MAIN.info("PackageMap does not match with chosenPackage {} - packageMap {}", JsonUtil.toString(chosenPackage), JsonUtil.toString(packageInfoMap));
            throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
        }
        return packageInfo;
    }

    private ApplicationForm createApplicationCache(String applicationId, String partnerId, OnboardingData<InitFormRequest, InitFormResponse> onboardingData, PackageInfo packageInfo, PackageCache packageCache, ServiceObInfo serviceObInfo) throws Exception {
        ApplicationForm applicationCache = new ApplicationForm();
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPartnerId(partnerId);
        applicationData.setApplicationId(applicationId);

        applicationData.setServiceId(onboardingData.getServiceId());
        applicationData.setPhoneNumber(getResource().getPhoneFormat().formatPhone11To10(onboardingData.getInitiator()));

        applicationData.setInitiator(onboardingData.getInitiator());
        applicationData.setAgentId(onboardingData.getInitiatorId());
        applicationData.setCreatedDate(System.currentTimeMillis());
        applicationData.setExpiredTimeInMillis(System.currentTimeMillis() + serviceObInfo.getPendingFormCacheTimeInMillis());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        applicationData.setStatus(ApplicationStatus.INIT_APPLICATION_FORM);
        applicationData.setState(ApplicationStatus.INIT_APPLICATION_FORM.getState());

        if (Utils.isNotEmpty(packageCache)) {
            applicationData.setCurrentCreditScore(packageCache.getMomoCreditScore());
        }
        if (packageInfo != null) {
            packageInfo.setLenderLogic(packageInfo.getLenderLogic());
            applicationData.setChosenPackage(packageInfo);
        }
        if (Utils.isNotEmpty(packageCache)
                && Utils.isNotEmpty(packageCache.getGetPackageResponse())
                && Utils.isNotEmpty(packageCache.getGetPackageResponse().getMerchantInfoRecord())
                && Utils.isNotEmpty(packageCache.getGetPackageResponse().getMerchantInfoRecord().getFirst())) {
            applicationData.setMerchantCode(packageCache.getGetPackageResponse().getMerchantInfoRecord().getFirst().getMerchantCode());
        }
        applicationCache.setApplicationData(applicationData);
        applicationCache.setRedirectTo(onboardingData.getProcessName());
        return applicationCache;
    }

    private String generateApplicationId(OnboardingData<InitFormRequest, InitFormResponse> onboardingData, ServiceObInfo serviceObInfo, PackageInfo packageInfo) {
        String nanoTime = String.valueOf(System.nanoTime());

        if (Utils.isNotEmpty(serviceObInfo.getServiceMerge()) && Utils.isNotEmpty(packageInfo)) {

            String prefix = getConfig().getPrefixSegmentMap().entrySet().stream()
                    .filter(entry -> Utils.nullToEmpty(packageInfo.getSegmentUser()).startsWith(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

            if (Utils.isNotEmpty(prefix)) {
                int deviation = onboardingData.getPartnerInfo().getShortName().length() - prefix.length();
                return String.format("%s%s%s", prefix, PrefixOnboarding.NEW.getCode(), nanoTime.substring(nanoTime.length() - Math.min(serviceObInfo.getContractLength() + deviation, nanoTime.length())));
            }

        }
        return String.format("%s%s%s", onboardingData.getPartnerInfo().getShortName(), PrefixOnboarding.NEW.getCode(), nanoTime.substring(nanoTime.length() - Math.min(serviceObInfo.getContractLength(), nanoTime.length())));
    }
}
