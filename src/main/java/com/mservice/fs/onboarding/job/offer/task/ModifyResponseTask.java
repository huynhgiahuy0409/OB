package com.mservice.fs.onboarding.job.offer.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ServiceObInfoTask;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.common.offer.OfferRequest;
import com.mservice.fs.onboarding.model.common.offer.OfferResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author hoang.thai on 10/30/2023
 */
public class ModifyResponseTask extends OnboardingTask<OfferRequest, OfferResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ModifyResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OfferRequest, OfferResponse> jobData) throws BaseException, Exception, ValidatorException {
        PackageCache packageCache = jobData.getTaskData(GetPackageTask.NAME).getContent();
        OfferResponse response = new OfferResponse();
        onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId()).getActionType().checkPackageFromAI(packageCache);
        List<PackageInfo> packageInfos = new ArrayList<>();
        List<PackageInfo> zeroInterestPackage = new ArrayList<>();
        fillPackage(jobData, packageInfos, zeroInterestPackage);
        response.setPackages(packageInfos);
        response.setZeroInterestPackages(zeroInterestPackage);
        response.setResultCode(CommonErrorCode.SUCCESS);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }


    private void fillPackage(OnboardingData<OfferRequest, OfferResponse> jobData, List<PackageInfo> packageInfos, List<PackageInfo> zeroPackageInfos) {
        PackageCache packageCache = jobData.getTaskData(GetPackageTask.NAME).getContent();
        if (Utils.isEmpty(packageCache)) {
            return;
        }
        OfferRequest request = jobData.getRequest();
        ServiceObInfo serviceObInfo = jobData.getTaskData(ServiceInfoTask.NAME).getContent();
        LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
        if (Utils.isEmpty(packageInfoMap)) {
            return;
        }
        Set<String> unavailablePackageNames = request.getUnavailablePackageNames();
        for (var entry : packageInfoMap.entrySet()) {
            PackageInfo packageInfo = entry.getValue();
            if (Utils.isNotEmpty(unavailablePackageNames) && unavailablePackageNames.contains(entry.getValue().getPackageName())) {
                Log.MAIN.info("Set UNAVAILABLE for packageCode {}", packageInfo.getPackageCode());
                packageInfo.setPackageStatus(PackageStatus.UNAVAILABLE.name());
            }
            if (OnboardingUtils.isZeroInterestPackage(packageInfo) && serviceObInfo.isApplyZeroInterest()) {
                Log.MAIN.info("APPLY ZERO PACKAGE [{}] ", packageInfo);
                zeroPackageInfos.add(packageInfo);
                continue;
            }
            packageInfos.add(packageInfo);
        }
    }

}
