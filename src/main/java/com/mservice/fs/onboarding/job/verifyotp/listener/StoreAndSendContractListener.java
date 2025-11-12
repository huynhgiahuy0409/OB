package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.common.config.ContractType;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.rabbit.receive.QueueListener;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.utils.Utils;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.Set;

public class StoreAndSendContractListener extends QueueListener<OnboardingData<VerifyOtpRequest, VerifyOtpResponse>, VerifyOtpRequest, VerifyOtpResponse, OnboardingConfig> {

    private static final String NAME = "STORE_SEND_CONTRACT";

    @Autowire(name = "OnboardingRabbitService")
    private RabbitSendingService onboardingRabbitService;

    @Autowire(name = "ServiceConfigInfo")
    DataService<ServiceObConfig> onboardingDataInfo;

    public StoreAndSendContractListener() {
        super(NAME);
    }


    @Override
    protected void processWithResponse(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData, byte[] bytes) throws BaseException, Exception {
        //Not return response form queue
    }

    @Override
    protected void processWithLateResponse(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData, byte[] bytes) throws BaseException, Exception {
        //Not return response form queue

    }

    @Override
    protected void processTimeout(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) throws BaseException, Exception {
        //Not return response form queue
    }

    @Override
    protected void error(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> verifyOtpRequestVerifyOtpResponseOnboardingData, Throwable throwable) {
        Log.MAIN.error("[StoreAndSendContractListener] Error when send job {}", OnboardingProcessor.CONTRACT, throwable);
    }

    @Override
    protected String createQueueRequest(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) throws BaseException, Exception {
        StoreContractRequest request = new StoreContractRequest();
        ApplicationData applicationData = onboardingData.getResponse().getApplicationData();
        request.setRequestId(onboardingData.getRequestId());
        request.setApplicationData(applicationData);
        request.setOtp(onboardingData.getRequest().getOtp());
        return Json.encode(request);
    }

    @Override
    protected Map<String, Object> createRequestHeaders(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        Map<String, Object> headers = super.createRequestHeaders(jobData);
        headers.put(Base.PROCESS_NAME_FIELD, OnboardingProcessor.CONTRACT);
        headers.put(Base.SERVICE_ID_FIELD_NAME, jobData.getServiceId());
        headers.put(Base.PARTNER_ID_FIELD_NAME, jobData.getPartnerId());
        headers.put(Base.USER, jobData.getInitiator());
        headers.put(Base.AGENT_ID, jobData.getInitiatorId());
        headers.put(Base.MODULE_FIELD_NAME, jobData.getModule());
        headers.put(Base.SOURCE, jobData.getSource());
        return headers;
    }

    @SneakyThrows
    @Override
    protected boolean isValidCondition(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> onboardingData) {
        if (onboardingData.isLoadFromCache()) {
            Log.MAIN.info("Response from cache - skip listener");
            return Boolean.FALSE;
        }
        String serviceId = onboardingData.getServiceId();
        String partnerId = onboardingData.getPartnerId();
        VerifyOtpResponse response = onboardingData.getResponse();
        if (!CommonErrorCode.SUCCESS.getCode().equals(response.getResultCode())) {
            Log.MAIN.info("ServiceId {} with partnerId {} does not generate contract with resultCode {} != 0", serviceId, partnerId, response.getResultCode());
            return Boolean.FALSE;
        }

        PartnerConfig partnerConfig = onboardingDataInfo.getData().getServiceObInfo(serviceId).getPartnerConfig(partnerId);
        if (partnerConfig == null) {
            Log.MAIN.info("ServiceId {} partnerId {} partnerConfig is null => does not config => dose not generate contract", serviceId, partnerId);
            return Boolean.FALSE;
        }

        Set<ContractType> contractTypes = partnerConfig.getTypeContracts();
        if (Utils.isEmpty(contractTypes)) {
            Log.MAIN.info("ServiceId {} - PartnerId {} does not config => dose not generate contract", serviceId, partnerId);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    protected RabbitSendingService getRabbitSendingService() {
        return onboardingRabbitService;
    }

}
