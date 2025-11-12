package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.checkstatus.task.ServiceObInfoTask;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ConsentConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.consent.ConsentRequest;
import com.mservice.fs.onboarding.model.consent.ConsentResponse;
import com.mservice.fs.rabbit.receive.QueueListener;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.utils.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class SendUserConsent extends QueueListener<OnboardingData<SubmitRequest, SubmitResponse>, SubmitRequest, SubmitResponse, OnboardingConfig> {

    private static final String MSG_TYPE = "OP_POST_IAM_ME";
    private static final String NAME = "SEND_USER_CONSENT";
    private static final String METHOD = "method";
    private static final String USER_PHONE = "user_phone";
    private static final String MINI_APP_ID = "miniAppId";
    private static final String PARTNER_CODE = "partnerCode";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Autowire(name = "userConsent")
    private RabbitSendingService rabbitConfirmPayment;

    public SendUserConsent() {
        super(NAME);
    }

    @Override
    protected boolean isValidCondition(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        ServiceObInfo serviceObInfo = getServiceObInfo(onboardingData);
        if (serviceObInfo.isMatchAction(Action.SEND_USER_CONSENT, onboardingData.getProcessName())) {
            return true;
        }
        return false;
    }

    @Override
    protected void processWithResponse(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, byte[] bytes) throws BaseException, Exception {
        Log.MAIN.info("[SendUserConsent] processWithResponse Received response: [{}]", new String(bytes));
        String consentResponseString = new String(bytes);
        ConsentResponse consentResponse = JsonUtil.fromString(consentResponseString, ConsentResponse.class);
        Integer errorCode = consentResponse.getErrorCode();
        if (!CommonErrorCode.SUCCESS.getCode().equals(errorCode)) {
            Log.MAIN.error("Response user consent fail errorCode {} - response {}", errorCode, consentResponseString);
        } else {
            Log.MAIN.info("Response user consent success {}", consentResponseString);
        }
    }

    @Override
    protected Map<String, Object> createRequestHeaders(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        Map<String, Object> headers = super.createRequestHeaders(onboardingData);
        ServiceObInfo serviceObInfo = getServiceObInfo(onboardingData);
        ConsentConfig consentConfig = serviceObInfo.getConsentConfig();

        headers.put(Base.PROCESS_NAME_FIELD, onboardingData.getProcessName());
        headers.put(Base.SERVICE_ID_FIELD_NAME, onboardingData.getServiceId());
        headers.put(Base.PARTNER_ID_FIELD_NAME, onboardingData.getPartnerId());
        headers.put(Base.AGENT_ID, onboardingData.getInitiatorId());
        headers.put(Base.MODULE_FIELD_NAME, onboardingData.getModule());
        headers.put(Base.SOURCE, onboardingData.getSource());
        headers.put(Base.TRACE_ID, onboardingData.getTraceId());

        headers.put(METHOD, "POST");
        try {
            headers.put(USER_PHONE, getResource().getPhoneFormat().formatPhone11To10(onboardingData.getInitiator()));
            headers.put(Base.USER, getResource().getPhoneFormat().formatPhone10To11(onboardingData.getInitiator()));
        } catch (IOException e) {
            Log.MAIN.error("Error when format user phone consent");
            throw new RuntimeException(e);
        }
        headers.put(MINI_APP_ID, consentConfig.getMiniAppId());
        headers.put(PARTNER_CODE, consentConfig.getPartnerCode());
        return headers;
    }

    @Override
    protected void processWithLateResponse(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, byte[] bytes) throws BaseException, Exception {
        Log.MAIN.info("[SendUserConsent] processWithLateResponse Received response: [{}]", new String(bytes));
    }

    @Override
    protected void processTimeout(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) throws BaseException, Exception {
        Log.MAIN.error("SendUserConsent timeout, Continue process..!!");
    }

    @Override
    protected void error(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, Throwable throwable) {
        Log.MAIN.error("[SendUserConsent] Error when send userConsent Message:", throwable);
    }

    @Override
    protected String createQueueRequest(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) throws BaseException, Exception {
        ServiceObInfo serviceObInfo = getServiceObInfo(onboardingData);
        ConsentConfig consentConfig = serviceObInfo.getConsentConfig();
        ConsentRequest.MomoMsg momoMsg = new ConsentRequest.MomoMsg();
        momoMsg.setRequestId(onboardingData.getRequestId());
        momoMsg.setMiniAppId(consentConfig.getMiniAppId());
        momoMsg.setAttributeList(consentConfig.getAttributeList());
        ConsentRequest consentRequest = new ConsentRequest(momoMsg, MSG_TYPE, onboardingData.getInitiator(), System.currentTimeMillis(), onboardingData.getRequestId());
        return Json.encode(consentRequest);
    }

    public ServiceObInfo getServiceObInfo(OnboardingData<SubmitRequest, SubmitResponse> onboardingData){
        ServiceObInfo serviceObInfo;
        try {
            serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
        } catch (ValidatorException | BaseException | Exception e) {
            Log.MAIN.error("Error when getServiceObInfo Message:", e);
            throw new RuntimeException(e);
        }
        return serviceObInfo;
    }

    @Override
    protected RabbitSendingService getRabbitSendingService() {
        return rabbitConfirmPayment;
    }
}
