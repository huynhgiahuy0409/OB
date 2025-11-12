package com.mservice.fs.onboarding.job.notinewpackage.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.notipackage.OnboardingNotiRequest;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.sof.queue.model.profile.OpenPlatformMsgRequest;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.sof.queue.service.GetUserProfileTask;
import com.mservice.fs.utils.Utils;

public class NotiGetUserProfile extends GetUserProfileTask<OnboardingData<OnboardingNotiRequest, OnboardingResponse>, OnboardingNotiRequest, OnboardingResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "GET_USER_PROFILE";

    @Autowire
    private RabbitSendingService getUserProfileService;

    public NotiGetUserProfile() {
        super(NAME);
    }

    @Override
    protected void validateUserProfileInfo(OnboardingData<OnboardingNotiRequest, OnboardingResponse> onboardingNotiRequestOnboardingResponseOnboardingData, TaskData taskData, UserProfileInfo userProfileInfo) throws ValidatorException, BaseException, Exception {

    }

    @Override
    protected OpenPlatformMsgRequest.OpenPlatformMsg buildRequest(OnboardingData<OnboardingNotiRequest, OnboardingResponse> onboardingData, OnboardingNotiRequest onboardingNotiRequest) {
        String callerId = getConfig().getOnboardingService();
        OpenPlatformMsgRequest.OpenPlatformMsg momoMsg = new OpenPlatformMsgRequest.OpenPlatformMsg();
        momoMsg.setCallerId(callerId);
        momoMsg.setPartnerCode(callerId);
        momoMsg.setAgentId(onboardingData.getInitiatorId());
        return momoMsg;
    }

    @Override
    protected RabbitSendingService getRabbitSendingService() {
        return getUserProfileService;
    }
}
