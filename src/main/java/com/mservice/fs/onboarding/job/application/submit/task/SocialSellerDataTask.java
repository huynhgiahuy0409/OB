package com.mservice.fs.onboarding.job.application.submit.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.connection.http.SocialSellerService;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.application.submit.IdentRequestLog;
import com.mservice.fs.onboarding.model.application.submit.SocialSellerRequest;
import com.mservice.fs.onboarding.model.application.submit.SocialSellerResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;

import java.io.IOException;

/**
 * @author hoang.thai
 * on 11/21/2023
 */
public class SocialSellerDataTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "GET_SOCIAL_SELLER_DATA";

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire(name = "SocialSellerData")
    private SocialSellerService socialSellerService;

    public SocialSellerDataTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> jobData) throws BaseException, Exception, ValidatorException {
        T request = jobData.getRequest();
        String serviceId = jobData.getServiceId();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
        if (!serviceObInfo.isMatchAction(Action.GET_SOCIAL_SELLER_DATA, jobData.getProcessName())) {
            Log.MAIN.info("ServiceId {} dose not need to get social seller data, by pass...", serviceId);
            finish(jobData, taskData);
            return;
        }
        UserProfileInfo userProfileInfo = jobData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        String socialSellerRequest = createSocialSellerRequest(jobData, request, serviceObInfo.getAiConfig(), userProfileInfo);
        SimpleHttpResponse socialSellerResponse = socialSellerService.callApi(socialSellerRequest, jobData.getBase());
        validateSocialSellerResponse(socialSellerResponse);
        taskData.setContent(socialSellerResponse.getContentStr());
        finish(jobData, taskData);
    }

    private void validateSocialSellerResponse(SimpleHttpResponse socialSellerResponse) throws BaseException, IOException {
        SocialSellerResponse.Data data = JsonUtil.fromByteArray(socialSellerResponse.getContent(), SocialSellerResponse.class).getData();
        if (data == null) {
            Log.MAIN.error("Response error with data: null");
            throw new BaseException(OnboardingErrorCode.RESPONSE_SOCIAL_SELLER_DATA_INVALID);
        }
    }

    private String createSocialSellerRequest(OnboardingData<T, R> jobData, T request, AiConfig aiConfig, UserProfileInfo userProfileInfo) throws JsonProcessingException {
        IdentRequestLog identRequestLog = IdentRequestLog.builder()
                .loanProductCode(aiConfig.getLoanProductCode())
                .productId(aiConfig.getProductId())
                .productGroup(aiConfig.getProductGroup())
                .partnerId(jobData.getPartnerId())
                .agentId(jobData.getInitiatorId())
                .nationalId(userProfileInfo.getPersonalIdKyc())
                .phoneNumber(jobData.getInitiator())
                .build();
        return JsonUtil.toString(SocialSellerRequest.builder()
                .requestId(request.getRequestId())
                .requestTimestamp(System.currentTimeMillis())
                .identRequest(identRequestLog)
                .build());
    }
}
