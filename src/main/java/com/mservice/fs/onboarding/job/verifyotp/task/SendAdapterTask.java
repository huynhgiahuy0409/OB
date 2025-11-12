package com.mservice.fs.onboarding.job.verifyotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterRequest;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SendAdapterTask extends OnboardingSendAdapterTask<VerifyOtpRequest, VerifyOtpResponse, VerifyOtpAdapterResponse> {

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final Set<ApplicationStatus> PARTNER_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    @Override
    protected String createAdapterRequest(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws Exception, BaseException, ValidatorException {
        VerifyOtpRequest request = jobData.getRequest();

        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpInfo = applicationData.getOtpInfo();

        UserProfileInfo userProfileInfo = null;
        if (getConfig().getVerifyOtpUpdateLinkS3Partners().contains(jobData.getPartnerId())) {
            userProfileInfo = jobData.getTaskData(VerifyOtpGetUserProfileTask.NAME).getContent();
            OnboardingUtils.setImageApplicationFromUserProfile(applicationData, userProfileInfo);
        }

        VerifyOtpAdapterRequest adapterRequest = new VerifyOtpAdapterRequest();
        adapterRequest.setRequestId(request.getRequestId());
        adapterRequest.setOtp(request.getOtp());
        adapterRequest.setOtpPartnerKey(otpInfo.getOtpPartnerKey());
        adapterRequest.setResultUrl(request.getResultUrl());
        adapterRequest.setMomoCreditScore(applicationForm.getApplicationData().getCurrentCreditScore());
        adapterRequest.setApplicationData(applicationData);
        adapterRequest.setDeviceName(request.getDeviceName());
        adapterRequest.setDeviceOS(request.getDeviceOS());

        Map<String, Object> partnerPayload = this.buildPayloadForPartner(userProfileInfo, jobData.getPartnerId(), this.getConfig().getPartnerFieldMap());
        adapterRequest.setPayload(partnerPayload);

        Log.MAIN.info("Request send to ADAPTER [{}]", adapterRequest);
        return adapterRequest.encode();
    }


    @Override
    protected void processAdapterResponse(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, VerifyOtpAdapterResponse adapterResponse) throws Exception, BaseException, ValidatorException {
        taskData.setContent(adapterResponse);
        finish(jobData, taskData);
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        Log.MAIN.error("[Verify otp] Partner time out");
        return CommonErrorCode.PARTNER_SERVICE_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        Log.MAIN.error("[Verify otp] Partner Runtime error");
        return CommonErrorCode.PARTNER_SERVICE_ERROR;
    }

    @Override
    protected String getPartnerId(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        return onboardingData.getRequest().getPartnerId();
    }

    @Override
    protected void fillUpDataToResponseWhenFailed(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, VerifyOtpAdapterResponse adapterResponse, VerifyOtpResponse response, Integer platformResultCode) {
        try {
            jobData.getTaskData(SendAdapterTask.NAME).setContent(adapterResponse);
            OtpConfig otpConfigInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                    .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

            ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
            ApplicationData applicationData = applicationForm.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();

            setDataResponse(response, otpInfo, otpConfigInfo, applicationData, jobData, adapterResponse);

        } catch (Exception | BaseException | ValidatorException e) {
            Log.MAIN.error("Get Otp Data Config Error, {}", e);
            throw new RuntimeException(e);
        }
    }

    protected void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, OtpConfig otpConfigInfo, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, VerifyOtpAdapterResponse adapterResponse) throws BaseException, ValidatorException, Exception {
        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
        applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_FAILED);
        applicationData.setState(ApplicationStatus.VERIFIED_OTP_FAILED.getState());
        applicationData.setReasonId(adapterResponse.getReasonId());
        applicationData.setReasonMessage(adapterResponse.getReasonMessage());
        if (Utils.isNotEmpty(adapterResponse.getPartnerApplicationId())) {
            Log.MAIN.info("Partner applicationId is not null", adapterResponse.getPartnerApplicationId());
            applicationData.setPartnerApplicationId(adapterResponse.getPartnerApplicationId());
        }

        ApplicationStatus partnerStatus = adapterResponse.getPartnerStatus();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (partnerStatus != null) {
            if (!PARTNER_STATUS.contains(partnerStatus)) {
                Log.MAIN.error("Error when partner status adapter not in [{}]", PARTNER_STATUS);
                throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
            }
            Log.MAIN.info("Update status by Partner");
            applicationData.setStatus(partnerStatus);
            if (serviceObInfo.isMatchAction(Action.BANNED_BY_PARTNER, jobData.getProcessName()) && partnerStatus == ApplicationStatus.REJECTED_BY_LENDER) {
                Log.MAIN.info("Banked application by partner !!!");
                applicationData.setState(ApplicationState.BANNED);
            }
        }
        response.setMaxGenerateTimes(maxGenerateOtpTimes);
        response.setMaxVerifyTimes(maxVerifyOtpTimes);
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setRemainingVerifyTimes(remainingVerifyTimes);
        response.setCurrentVerifyTimes(currentVerifyTimes);
        response.setApplicationData(applicationData);
        response.setApplicationId(applicationData.getApplicationId());
    }

    private boolean isAllowForward(UserProfileInfo userProfileInfo, OnboardingConfig config, String partnerId) {
        boolean result = false;

        if (Utils.isNotEmpty(userProfileInfo) && config.getPartnerFieldMap().containsKey(partnerId) && !config.getPartnerFieldMap().get(partnerId).isEmpty()) {
            return true;
        }

        return result;
    }

    private Map<String, Object> buildPayloadForPartner(UserProfileInfo userProfileInfo, String partnerId, Map<String, List<String>> partnerFieldMap) throws BaseException {
        Map<String, Object> payload = new HashMap<>();

        if (!this.isAllowForward(userProfileInfo, getConfig(), partnerId)) {
            return payload;
        }

        List<String> allowedFields = partnerFieldMap.get(partnerId);

        for (String field : allowedFields) {
            try {
                Field f = UserProfileInfo.class.getDeclaredField(field);
                f.setAccessible(true);
                Object value = f.get(userProfileInfo);

                if (value != null && !value.toString().trim().isEmpty()) {
                    payload.put(field, value);
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new BaseException(CommonErrorCode.SYSTEM_BUG);
            }
        }

        return payload;
    }
}
