package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.model.status.SegmentData;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hoang.thai on 10/30/2023
 */
public class ModifyResponseTask extends OnboardingTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private final Set<ApplicationStatus> LOCK_OTP_SIGN = Set.of(ApplicationStatus.REJECTED_BY_LIMITED_VERIFIED_OTP_SIGN_EXCEED, ApplicationStatus.REJECTED_BY_LIMITED_GENERATED_OTP_SIGN_EXCEED);

    public ModifyResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws BaseException, Exception, ValidatorException {
        OnboardingStatusResponse response = new OnboardingStatusResponse();
        PackageCache packageCache = jobData.getTaskData(CheckStatusPackageTask.NAME).getContent();
        boolean isEmptyPackage = true;
        if (packageCache != null) {
            LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
            List<PackageInfo> packageInfos = new ArrayList<>();
            List<PackageInfo> zeroPackageInfos = new ArrayList<>();
            isEmptyPackage = checkPackageAndCreateListPackage(packageInfoMap, packageInfos, zeroPackageInfos, jobData);
            response.setPackages(packageInfos);
            response.setZeroInterestPackages(zeroPackageInfos);
        }
        modifyResponse(taskData, jobData, response);
        fillUpResponse(jobData, response);
        response.setEmptyPackage(isEmptyPackage);
        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        response.setUserProfileInfo(userProfileInfo);
        response.setResultCode(CommonErrorCode.SUCCESS);
        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    protected void modifyResponse(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, OnboardingStatusResponse response) throws BaseException, ValidatorException, Exception {
        modifyApplicationData(taskData, jobData);
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (!serviceObInfo.isRecheckPendingForm()) {
            Log.MAIN.info("Service not need recheck pending form - skip task");
            return;
        }

        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        Set<String> availablePackageCodes = new HashSet<>();
        PackageCache packageCache = jobData.getTaskData(CheckStatusPackageTask.NAME).getContent();
        if (Utils.isNotEmpty(packageCache)) {

            LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();
            packageInfoMap.forEach((key, value) -> {
                if (PackageStatus.AVAILABLE.name().equals(value.getPackageStatus())) {
                    availablePackageCodes.add(value.getPackageCode());
                }
            });
        }

        Iterator<ApplicationForm> iterators = checkStatusData.getApplicationForms().iterator();
        while (iterators.hasNext() && Utils.isNotEmpty(packageCache)) {
            ApplicationForm appForm = iterators.next();
            ApplicationData applicationData = appForm.getApplicationData();

            if (!availablePackageCodes.contains(applicationData.getChosenPackage().getPackageCode())) {
                Log.MAIN.info("Chosen package not exist in list package response from AI");
                iterators.remove();
                taskData.setContent(checkStatusData);
            }
        }
    }

    private void modifyApplicationData(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) {
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        Iterator<ApplicationForm> iterators = checkStatusData.getApplicationForms().iterator();
        while (iterators.hasNext()) {
            ApplicationForm appForm = iterators.next();
            ApplicationData applicationData = appForm.getApplicationData();
            long currentMillis = System.currentTimeMillis();
            if (ApplicationState.LOCK.equals(applicationData.getState()) && currentMillis >= applicationData.getOtpInfo().getUnlockOtpTimeInMillis()) {
                Log.MAIN.info("Start reset OTP info for: [{}]!!!", Json.encode(applicationData));

                ApplicationStatus status = ApplicationStatus.GENERATED_OTP;
                ApplicationStatus applicationStatus = applicationData.getStatus();
                if (LOCK_OTP_SIGN.contains(applicationStatus)) {
                    status = ApplicationStatus.GENERATED_OTP_SIGN;
                }
                applicationData.setStatus(status);
                applicationData.setState(status.getState());

                OtpInfo otpInfo = applicationData.getOtpInfo();
                otpInfo.setCurrentTimesGenerate(0);
                otpInfo.setCurrentTimesVerify(0);
                otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());

                checkStatusData.setModifyApplicationForm(appForm);
                continue;
            }
            if (ApplicationState.BANNED.equals(applicationData.getState()) && currentMillis >= applicationData.getExpiredTimeInMillis()) {
                Log.MAIN.info("Remove pending form BANKED [{}]", applicationData);
                checkStatusData.setModifyApplicationForm(appForm);
                iterators.remove();
            }
        }
    }

    public static void fillUpResponse(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData, OnboardingStatusResponse response) {
        SegmentData segmentData = jobData.getTaskData(SegmentUserTask.NAME).getContent();
        UserType userType = segmentData.getUserType();
        CheckStatusData checkStatusData = jobData.getTaskData(CacheTask.NAME).getContent();
        response.setPendingForms(checkStatusData.getApplicationForms());
        response.setUserType(userType);
        response.setSubmittedForms(segmentData.getSubmittedForms());
        response.setActiveForms(segmentData.getActiveFormsForms());
        response.setClosedForms(segmentData.getClosedForms());
        response.setWaitRoutingForms(segmentData.getWaitRoutingForms());
        Map<String, ApplicationForm> applicationFormMap = checkStatusData.getApplicationForms()
                .stream()
                .collect(Collectors.toMap(applicationForm -> applicationForm.getApplicationData().getApplicationId(), applicationForm -> applicationForm));
        for (ApplicationDataLite applicationDataLite : segmentData.getPendingStateForms()) {
            if (!applicationFormMap.containsKey(applicationDataLite.getApplicationId())
                    && Constant.REVERT_SIGN_STATUS.contains(applicationDataLite.getStatus())) {
                response.getSubmittedForms().add(applicationDataLite);
            }
        }
    }

    /**
     * Create list package for response and check is package empty?
     *
     * @param packageInfoMap
     * @param packageInfos
     * @return
     */
    private boolean checkPackageAndCreateListPackage(Map<String, PackageInfo> packageInfoMap, List<PackageInfo> packageInfos, List<PackageInfo> zeroPackageInfos, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws BaseException, ValidatorException, Exception {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (Utils.isEmpty(packageInfoMap)) {
            return true;
        }
        boolean isEmptyPackage = true;
        for (Map.Entry<String, PackageInfo> entry : packageInfoMap.entrySet()) {
            PackageInfo packageInfo = entry.getValue();
            if (PackageStatus.AVAILABLE.name().equals(packageInfo.getPackageStatus())) {
                isEmptyPackage = false;
            }
            if (OnboardingUtils.isZeroInterestPackage(packageInfo) && serviceObInfo.isApplyZeroInterest()) {
                Log.MAIN.info("APPLY ZERO PACKAGE [{}] ", packageInfo);
                zeroPackageInfos.add(packageInfo);
                continue;
            }
            packageInfos.add(packageInfo);
        }
        return isEmptyPackage;
    }

}
