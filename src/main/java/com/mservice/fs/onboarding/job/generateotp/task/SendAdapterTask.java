package com.mservice.fs.onboarding.job.generateotp.task;

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
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterRequest;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpRequest;
import com.mservice.fs.onboarding.model.generateotp.GenerateOtpResponse;
import com.mservice.fs.processor.TaskData;
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

        ApplicationForm applicationForm = getApplicationForm(jobData);
        ApplicationData applicationData = applicationForm.getApplicationData();

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
        ApplicationForm applicationForm = getApplicationForm(jobData);
        ApplicationData applicationData = applicationForm.getApplicationData();
        updatePartnerApplicationId(adapterResponse, applicationData);
        taskData.setContent(adapterResponse);
        finish(jobData, taskData);
    }

    @Override
    protected void fillUpDataToResponseWhenFailed(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData, GenerateOtpAdapterResponse adapterResponse, GenerateOtpResponse response, Integer platformResultCode) {
        try {
            jobData.getTaskData(SendAdapterTask.NAME).setContent(adapterResponse);
            OtpConfig otpConfig = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                    .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

            ApplicationForm applicationForm = getApplicationForm(jobData);
            ApplicationData applicationData = applicationForm.getApplicationData();

            setDataResponse(response, otpConfig, applicationData, adapterResponse);
        } catch (BaseException | Exception | ValidatorException e) {
            Log.MAIN.error("Get Otp Data Config Error, {}", e);
            throw new RuntimeException(e);
        }
    }

    private void setDataResponse(GenerateOtpResponse response, OtpConfig otpConfigInfo, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) {
        applicationData.setStatus(ApplicationStatus.GENERATED_OTP);
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

    private ApplicationForm getApplicationForm(OnboardingData<GenerateOtpRequest, GenerateOtpResponse> jobData) {
        return jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
    }
}
