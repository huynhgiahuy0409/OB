package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.api.submit.SubmitAdapterRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;
import lombok.SneakyThrows;

import java.util.Set;

/**
 * @author hoang.thai
 * on 11/22/2023
 */
public abstract class OnboardingSubmitTask<T extends ConfirmRequest, R extends SubmitResponse> extends OnboardingSendAdapterTask<T, R, GenerateOtpAdapterResponse> {

    @Autowire(name = "ServiceConfigInfo")
    protected DataService<ServiceObConfig> onboardingDataInfo;

    protected static final int DEFAULT_TIME_GENERATE_OTP = 0;
    protected static final int DEFAULT_CURRENT_TIMES_VERIFY_OTP = 0;

    private static final Set<ApplicationStatus> PARTNER_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO, ApplicationStatus.APPROVED_BY_LENDER, ApplicationStatus.REVIEW_BY_LENDER);

    @Override
    protected String createAdapterRequest(OnboardingData<T, R> jobData) throws Exception, BaseException, ValidatorException {
        T request = jobData.getRequest();
        // suggest auto call adapter
        ApplicationForm applicationForm = getApplicationForm(jobData);
        SubmitAdapterRequest submitAdapterRequest = new SubmitAdapterRequest();
        submitAdapterRequest.setRequestId(jobData.getRequestId());
        submitAdapterRequest.setSocialSellerData(getSocialSellerData(jobData));
        submitAdapterRequest.setPaymentInfo(getPaymentInfo(jobData));
        submitAdapterRequest.setApplicationData(applicationForm.getApplicationData());
        submitAdapterRequest.setMomoCreditScore(applicationForm.getApplicationData().getCurrentCreditScore());
        submitAdapterRequest.setDeviceId(request.getDeviceId());
        submitAdapterRequest.setIpAddress(request.getIpAddress());
        submitAdapterRequest.setLocation(request.getLocation());
        submitAdapterRequest.setDeviceName(request.getDeviceName());
        submitAdapterRequest.setDeviceOS(request.getDeviceOS());
        Log.MAIN.info("Request send to ADAPTER [{}]", submitAdapterRequest);
        return JsonUtil.toString(submitAdapterRequest);
    }

    protected String getSocialSellerData(OnboardingData<T, R> jobData) {
        return null;
    }

    protected String getPaymentInfo(OnboardingData<T, R> jobData) {
        return null;
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) throws BaseException, ValidatorException, Exception;

    @Override
    protected void processAdapterResponse(TaskData taskData, OnboardingData<T, R> jobData, GenerateOtpAdapterResponse adapterDefaultResponse) throws Exception, BaseException, ValidatorException {
        ServiceObInfo serviceObInfo = this.onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        updateApplicationData(jobData, applicationData, adapterDefaultResponse);
        OtpInfo otpInfo = applicationData.getOtpInfo();
        String adapterPartnerKey = adapterDefaultResponse.getOtpPartnerKey();
        String webViewLink = adapterDefaultResponse.getWebViewLink();
        boolean otpFlow = adapterDefaultResponse.isGenerateOtp();
        if (Utils.isNotEmpty(adapterPartnerKey)
                || Utils.isNotEmpty(webViewLink)
                || Boolean.TRUE.equals(otpFlow)) {
            processInGenerateOtpFlow(jobData, applicationData, adapterDefaultResponse);
        } else if (Utils.isEmpty(adapterDefaultResponse.getPartnerStatus())) {
            processNotInGenerateOtpFlow(applicationData, adapterDefaultResponse);
        }
        String adapterReferenceId = adapterDefaultResponse.getReferenceId();
        if (Utils.isNotEmpty(adapterReferenceId)) {
            Log.MAIN.info("Got referenceId from adapter, do set applicationId from [{}] to [{}]", applicationData.getApplicationId(), adapterReferenceId);
            applicationData.setApplicationId(adapterReferenceId);
        }
        applicationData.setOtpInfo(otpInfo);
        taskData.setContent(adapterDefaultResponse);
        finish(jobData, taskData);
    }

    protected void processInGenerateOtpFlow(OnboardingData<T, R> jobData, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) throws BaseException, ValidatorException, Exception {
    }

    protected void processNotInGenerateOtpFlow(ApplicationData applicationData, GenerateOtpAdapterResponse adapterDefaultResponse) {

    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        Log.MAIN.error("[Final-submit] Partner time out");
        return CommonErrorCode.PARTNER_SERVICE_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        Log.MAIN.error("[Final-submit] Partner Runtime error");
        return CommonErrorCode.PARTNER_SERVICE_ERROR;
    }

    @Override
    protected String getKeyRouting(OnboardingData<T, R> jobData) {
        return jobData.getPartnerId();
    }

    @SneakyThrows
    @Override
    protected void fillUpDataToResponseWhenFailed(OnboardingData<T, R> jobData, GenerateOtpAdapterResponse adapterResponse, R response, Integer platformResultCode) {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        updateApplicationData(jobData, applicationData, adapterResponse);
        response.setApplicationData(applicationData);
        jobData.getTaskData(OnboardingSubmitTask.NAME).setContent(adapterResponse);

    }


    private void updateApplicationData(OnboardingData<T, R> jobData, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) throws BaseException, ValidatorException, Exception {
        applicationData.setReasonId(adapterResponse.getReasonId());
        applicationData.setReasonMessage(adapterResponse.getReasonMessage());
        ApplicationStatus partnerStatus = adapterResponse.getPartnerStatus();
        if (partnerStatus == null) {
            Log.MAIN.info("Got partner status is null, do nothing !!! ");
            return;
        }
        if (!PARTNER_STATUS.contains(partnerStatus)) {
            Log.MAIN.error("Error when partner status adapter not in [{}]", PARTNER_STATUS);
            throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
        }

        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        Log.MAIN.info("Set app status to [{}]", partnerStatus);
        applicationData.setStatus(partnerStatus);
        applicationData.setState(partnerStatus.getState());
        if (serviceObInfo.isMatchAction(Action.BANNED_BY_PARTNER, jobData.getProcessName()) && partnerStatus == ApplicationStatus.REJECTED_BY_LENDER) {
            Log.MAIN.info("Set state to banked");
            applicationData.setState(ApplicationState.BANNED);
        }
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
    }

    @Override
    protected boolean isAllowedSendAdapter(OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.SEND_ADAPTER_SUBMIT, jobData.getProcessName())) {
            return true;
        }
        processInitOtpInfoNotSendAdapterFlow(jobData, serviceObInfo);
        return false;
    }

    private void processInitOtpInfoNotSendAdapterFlow(OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo) throws BaseException, Exception, ValidatorException {
        {
            ApplicationForm applicationForm = getApplicationForm(jobData); //getApplicationForm for init otpInfo
            ApplicationData applicationData = applicationForm.getApplicationData();
            OtpInfo otpInfo = applicationData.getOtpInfo();
            if (serviceObInfo.isGenerateOtpWhenSubmit()) {
                Log.MAIN.info("Generate OTP when submit service {} agentId {}", jobData.getServiceId(), jobData.getInitiatorId());
                processInGenerateOtpFlow(jobData, applicationData, null);
            } else {
                Log.MAIN.info("Not Generate OTP when submit service {} agentId {}", jobData.getServiceId(), jobData.getInitiatorId());
                processNotInGenerateOtpFlow(applicationData, null);
            }
            applicationData.setOtpInfo(otpInfo);
        }
    }

}
