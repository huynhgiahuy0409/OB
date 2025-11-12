package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.model.MessageRpl;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.PlatformResponse;
import com.mservice.fs.onboarding.model.OfferPackage;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.getpackage.GetPackageListRequest;
import com.mservice.fs.onboarding.model.getpackage.GetPackageListResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import javax.script.ScriptException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LendingConfiguredPackageTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingSendPlatformTask<T, R> {

    public static final TaskName NAME = () -> "SEND_LENDING_PACKAGE";

    @Autowire
    private ProxyGrpcClient lendingPackageGrpcClient;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final int LIST_PACKAGE_EMPTY_METRIC_CODE = 6000;

    public LendingConfiguredPackageTask() {
        super(NAME);
    }

    @Override
    protected void processWithResponse(OnboardingData<T, R> platformData, TaskData taskData, MessageRpl grpcResponse) throws BaseException, Exception, ValidatorException {
        Log.MAIN.info("Got Response ResultCode:[{}], ResultMessage:[{}]",
                grpcResponse.getResultCode(), grpcResponse.getResultMessage());

        GetPackageListResponse pkgListResp =
                (GetPackageListResponse) createCrossPlatformResponse(platformData, grpcResponse.getPayload());
        List<PackageInfo> lendingPackages = pkgListResp.getData();

        PackageCache packageCache = platformData.getTaskData(PackageAiTask.NAME).getContent();
        GetPackageResponse aiResponse = packageCache.getGetPackageResponse();
        List<OfferPackage> offerPackages = packageCache.getOfferPackagesNotConfiguredInDb();
        ServiceObInfo serviceObInfo = getServiceObInfo(platformData, onboardingDataInfo);

        Map<String, PackageInfo> lendingPackageMap = lendingPackages.stream()
                .collect(Collectors.toMap(PackageInfo::getPackageCode, Function.identity()));

        LinkedHashMap<String, PackageInfo> packageInfoMap = Utils.coalesce(packageCache.getPackageInfoMap(), new LinkedHashMap<>());

        for (OfferPackage offer : offerPackages) {
            PackageInfo pkgInfo = lendingPackageMap.get(offer.getPackageCode());

            if (pkgInfo == null) {
                Log.MAIN.error("Package with code '{}' not found in the lending package.", offer.getPackageCode());
                throw new BaseException(CommonErrorCode.SYSTEM_BUG);
            }

            enrichPackageInfo(pkgInfo, offer, aiResponse, serviceObInfo);
            serviceObInfo.getActionType().handleWhenAddPackageMap(packageInfoMap, pkgInfo);
        }

        Log.MAIN.info("PackageInfos: {}", packageInfoMap);
        packageCache.setPackageInfoMap(packageInfoMap);
        addMetric(taskData, aiResponse.getResponseCode(), packageInfoMap, platformData);
        finish(platformData, taskData);
    }

    private void enrichPackageInfo(PackageInfo pkgInfo,
                                   OfferPackage offer,
                                   GetPackageResponse aiResp,
                                   ServiceObInfo serviceObInfo) throws ScriptException {

        pkgInfo.setLenderLogic(aiResp.getLenderLogic());
        pkgInfo.setRank(offer.getRank());
        pkgInfo.setPackageStatus(offer.getPackageStatus());
        pkgInfo.setSegmentUser(aiResp.getSegmentUser());
        
        OnboardingUtils.setInterestRate(getConfig().getWhiteListPackageInterestRate(), pkgInfo);

        if (serviceObInfo.isDoCalculateDueDate()) {
            pkgInfo.setDueDay(OnboardingUtils.getDueDate(serviceObInfo, offer.getLenderId()));
        }
        if(Utils.isNotEmpty(pkgInfo.getEmi())){
            pkgInfo.setDailyEquivalentAmount(OnboardingUtils.computeDailyEquivalentAmount(pkgInfo.getEmi()));
        }
    }

    @Override
    protected boolean isActive(OnboardingData<T, R> platformData) {
        PackageCache packageCache = platformData.getTaskData(PackageAiTask.NAME).getContent();
        return Utils.isNotEmpty(packageCache)
                && Utils.isNotEmpty(packageCache.getOfferPackagesNotConfiguredInDb())
                && Utils.isNotEmpty(getConfig().getLendingServiceProductCodeMap().get(platformData.getServiceId()));
    }

    @Override
    protected String getProcessName(OnboardingData<T, R> platformData) {
        return "onboarding-get-package-list";
    }

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<T, R> trOnboardingData) {
        return lendingPackageGrpcClient;
    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<T, R> trOnboardingData) {
        GetPackageListRequest request = new GetPackageListRequest();
        String loanProductCode = getConfig().getLendingServiceProductCodeMap().get(trOnboardingData.getServiceId());
        request.setRequestId(trOnboardingData.getRequestId());
        request.setLoanProductCode(loanProductCode);
        return request.toByteArrays();
    }

    @Override
    protected PlatformResponse createCrossPlatformResponse(OnboardingData<T, R> trOnboardingData, String s) throws Exception {
        return JsonUtil.fromString(s, GetPackageListResponse.class);
    }

    protected ServiceObInfo getServiceObInfo(OnboardingData<T, R> jobData, DataService<ServiceObConfig> onboardingDataInfo) throws BaseException, ValidatorException, Exception {
        return onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
    }

    private void addMetric(TaskData taskData, Integer responseAiCode, LinkedHashMap<String, PackageInfo> packageInfoMap, OnboardingData<T, R> jobData) {
        if (OnboardingUtils.isPackageEmpty(packageInfoMap)) {
            Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {} with metric code {}", responseAiCode, packageInfoMap, LIST_PACKAGE_EMPTY_METRIC_CODE);
            taskData.setResultCode(LIST_PACKAGE_EMPTY_METRIC_CODE);
        } else {
            String lenderId = packageInfoMap.values().iterator().next().getLenderId();
            Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {} with metric code {}", responseAiCode, packageInfoMap, lenderId);
            jobData.updateTagData(Constant.PACKAGE_TAG_NAME, lenderId);
            Log.MAIN.info("Add metric for package Info with responseAiCode - {}, packageInfoMap {}", responseAiCode, packageInfoMap);
            taskData.setResultCode(responseAiCode);
        }
    }

}
