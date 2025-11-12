package com.mservice.fs.onboarding.job.pendingform.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
public class ModifyResponseTask extends OnboardingTask<PendingFormRequest, PendingFormResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";

    public ModifyResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<PendingFormRequest, PendingFormResponse> jobData) throws BaseException, Exception, ValidatorException {
        PendingFormResponse response = new PendingFormResponse();
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationCacheTask.NAME).getContent();
        PackageCache packageCache = jobData.getTaskData(PendingFormPackageTask.NAME).getContent();
        validatePackage(applicationForm, packageCache);
        OnboardingUtils.createDataForEmptyPackage(applicationForm, packageCache);
        response.setRedirectTo(applicationForm.getRedirectTo());
        response.setApplicationData(applicationForm.getApplicationData());
        response.setResultCode(CommonErrorCode.SUCCESS);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    private void validatePackage(ApplicationForm applicationForm, PackageCache packageCache) throws Exception, BaseException {
        //CLO do not have package
        if (packageCache != null && packageCache.getPackageInfoMap() != null) {
            LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
            Set<String> lenderIds = new HashSet<>();
            Set<String> availablePackageCodes = new HashSet<>();
            packageInfoMap.forEach((key, value) -> {
                if (PackageStatus.AVAILABLE.name().equals(value.getPackageStatus())) {
                    availablePackageCodes.add(value.getPackageCode());
                }
                lenderIds.add(value.getLenderId());
            });
            if (Utils.isEmpty(availablePackageCodes)) {
                Log.MAIN.info("List package is empty");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
            }
            if (!availablePackageCodes.contains(applicationForm.getApplicationData().getChosenPackage().getPackageCode())) {
                Log.MAIN.info("Chosen package not exist in list package response from AI");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }
            if (!lenderIds.contains(applicationForm.getApplicationData().getChosenPackage().getLenderId())) {
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
            }
        }
    }
}
