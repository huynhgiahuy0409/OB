package com.mservice.fs.onboarding.job.checkwhitelistpackage.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.PackageAiTask;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.processor.TaskData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author phat.duong
 * on 12/20/2024
 **/
public class BuildResponseTask extends OnboardingTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final TaskName NAME = () -> "BUILD_RESPONSE";

    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        OnboardingStatusResponse response = new OnboardingStatusResponse();
        PackageCache packageCache = onboardingData.getTaskData(PackageAiTask.NAME).getContent();
        if (packageCache != null) {
            LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
            List<PackageInfo> packageInfos = checkPackageAndCreateListPackage(packageInfoMap);
            response.setPackages(packageInfos);
        }
        response.setResultCode(CommonErrorCode.SUCCESS);
        onboardingData.setResponse(response);
        finish(onboardingData, taskData);
    }

    private List<PackageInfo> checkPackageAndCreateListPackage(Map<String, PackageInfo> packageInfoMap) {
        List<PackageInfo> packageInfos = new ArrayList<>();
        for (Map.Entry<String, PackageInfo> entry : packageInfoMap.entrySet()) {
            PackageInfo packageInfo = entry.getValue();
            if (PackageStatus.AVAILABLE.name().equals(packageInfo.getPackageStatus())) {
                packageInfos.add(packageInfo);
            }
        }
        return packageInfos;
    }
}
