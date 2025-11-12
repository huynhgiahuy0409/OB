package com.mservice.fs.onboarding.job.sign.generateotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterRequest;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

public class SendAdapterTask extends OnboardingSendAdapterTask<GenerateOtpRequest, GenerateOtpResponse, GenerateOtpAdapterResponse> {

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected String getPartnerId(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> onboardingData) {
        return onboardingData.getRequest().getPartnerId();
    }

    @Override
    protected String createAdapterRequest(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) throws Exception, BaseException, ValidatorException {

        GenerateOtpRequest request = jobData.getRequest();

        ApplicationData applicationData = getApplicationData(jobData);
        if (getConfig().getGenerateOtpUpdateLinkS3Partners().contains(jobData.getPartnerId())) {
            UserProfileInfo userProfileInfo = jobData.getTaskData(OtpGetUserProfileTask.NAME).getContent();
            OnboardingUtils.setImageApplicationFromUserProfile(applicationData, userProfileInfo);
        }
        GenerateOtpAdapterRequest adapterRequest = new GenerateOtpAdapterRequest();
        adapterRequest.setRequestId(request.getRequestId());
        adapterRequest.setApplicationData(applicationData);
        adapterRequest.setDeviceName(request.getDeviceName());
        adapterRequest.setDeviceOS(request.getDeviceOS());

        Log.MAIN.info("Request send to ADAPTER [{}]", adapterRequest);
        return adapterRequest.encode();
    }

    @Override
    protected void processAdapterResponse(TaskData taskData, OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, GenerateOtpAdapterResponse adapterResponse) throws Exception, BaseException, ValidatorException {
        ApplicationData applicationData = getApplicationData(jobData);
        applicationData.setReasonMessage(adapterResponse.getReasonMessage());
        updatePartnerApplicationId(adapterResponse, applicationData);
        taskData.setContent(adapterResponse);
        finish(jobData, taskData);
    }

    @Override
    protected void fillUpDataToResponseWhenFailed(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, GenerateOtpAdapterResponse adapterResponse, GenerateOtpResponse response, Integer platformResultCode) {
        try {
            jobData.getTaskData(SendAdapterTask.NAME).setContent(adapterResponse);
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
            ApplicationData applicationData = getApplicationData(jobData);
            setDataResponse(jobData, serviceObInfo, applicationData, adapterResponse);
        } catch (BaseException | Exception | ValidatorException e) {
            Log.MAIN.error("Get Otp Data Config Error, {}", e);
            throw new RuntimeException(e);
        }
    }

    private void setDataResponse(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, ServiceObInfo serviceObInfo, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) throws BaseException {
        if (adapterResponse.isClearPendingForm()) {

            GenerateOtpResponse response = jobData.getResponse();
            applicationData.setStatus(ApplicationStatus.APPROVED_BY_LENDER);
            applicationData.setState(ApplicationStatus.APPROVED_BY_LENDER.getState());
            response.setApplicationId(applicationData.getApplicationId());
            return;
        }

        if (serviceObInfo.isMatchAction(Action.UPDATE_WHEN_FAIL, jobData.getProcessName())) {
            GenerateOtpResponse response = jobData.getResponse();
            OtpConfig otpConfigInfo = serviceObInfo.getPartnerConfig(jobData.getPartnerId()).getOtpConfig();
            applicationData.setStatus(ApplicationStatus.GENERATED_OTP_SIGN);
            applicationData.setState(ApplicationState.PENDING);
            updatePartnerApplicationId(adapterResponse, applicationData);

            OtpInfo otpInfo = applicationData.getOtpInfo();

            response.setCurrentGenerateTimes(otpInfo.getCurrentTimesGenerate() + 1);
            response.setCurrentVerifyTimes(otpInfo.getCurrentTimesVerify());
            response.setMaxGenerateTimes(otpConfigInfo.getMaxGenerateOtpTimes());
            response.setMaxVerifyTimes(otpConfigInfo.getMaxVerifyOtpTimes());
            response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
            response.setApplicationId(applicationData.getApplicationId());
        }
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        Log.MAIN.error("[Generate otp] Partner time out");
        return CommonErrorCode.PARTNER_SERVICE_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        Log.MAIN.error("[Generate otp] Partner Runtime error");
        return CommonErrorCode.PARTNER_SERVICE_ERROR;
    }

    private void updatePartnerApplicationId(GenerateOtpAdapterResponse adapterResponse, ApplicationData applicationData) {
        String partnerApplicationId = adapterResponse.getPartnerApplicationId();
        if (Utils.isNotEmpty(partnerApplicationId)) {
            Log.MAIN.info("Update partnerApplicationId {}", partnerApplicationId);
            applicationData.setPartnerApplicationId(partnerApplicationId);
        }
    }

    private ApplicationData getApplicationData(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        return jobData.getTaskData(GetApplicationTask.NAME).getContent();
    }
}
