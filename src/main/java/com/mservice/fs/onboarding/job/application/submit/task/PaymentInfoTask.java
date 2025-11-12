package com.mservice.fs.onboarding.job.application.submit.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.http.PaymentInfoService;
import com.mservice.fs.onboarding.enums.AIDataLogType;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.submit.PaymentInfoRequest;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 11/21/2023
 */
public class PaymentInfoTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_PAYMENT_INFO";

    @Autowire(name = "PaymentInfo")
    private PaymentInfoService paymentInfoService;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public PaymentInfoTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        T request = jobData.getRequest();
        String serviceId = jobData.getServiceId();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        if (!serviceObInfo.isMatchAction(Action.GET_PAYMENT_INFO, jobData.getProcessName())) {
            Log.MAIN.info("ServiceId {} does not need to get payment info, by pass...", serviceId);
            finish(jobData, taskData);
            return;
        }
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        String paymentInfoRequest = createPaymentInfoRequest(jobData, request, serviceObInfo.getAiConfig(), applicationForm);
        String paymentInfoResponse = paymentInfoService.callApi(paymentInfoRequest, jobData.getBase());
        validatePaymentInfoResponse(paymentInfoResponse);
        taskData.setContent(paymentInfoResponse);
        finish(jobData, taskData);
    }

    private void validatePaymentInfoResponse(String paymentInfoResponse) throws BaseException {
        if (Utils.isEmpty(paymentInfoResponse)) {
            Log.MAIN.error("Call partner info empty {}", paymentInfoResponse);
            throw new BaseException(OnboardingErrorCode.RESPONSE_PAYMENT_INFO_INVALID);
        }
    }


    private String createPaymentInfoRequest(OnboardingData<T, R> jobData, T request, AiConfig aiConfig, ApplicationForm applicationForm) throws JsonProcessingException {
        return JsonUtil.toString(PaymentInfoRequest.builder()
                .requestTimestamp(System.currentTimeMillis())
                .requestId(request.getRequestId())
                .agentId(Long.parseLong(jobData.getInitiatorId()))
                .partnerId(jobData.getPartnerId())
                .dataLogType(AIDataLogType.PAYMENT_INTO.name())
                .messageType(AIMessageType.TOTAL_PAYMENT_INFO_EVENT.name())
                .orderId(applicationForm.getApplicationData().getApplicationId())
                .platformId(aiConfig.getPlatformId())
                .requestSource(aiConfig.getSourceId())
                .loanProductCode(aiConfig.getLoanProductCode().toUpperCase())
                .build());
    }
}
