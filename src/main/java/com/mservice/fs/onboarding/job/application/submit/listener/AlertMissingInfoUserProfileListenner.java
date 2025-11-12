package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.submit.task.SubmitGetUserProfileTask;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.utils.GChatSevice;
import com.mservice.fs.onboarding.utils.UserProfileInfoValidator;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author tuan.tran6
 * on 09/13/2024
 */
public class AlertMissingInfoUserProfileListenner<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingListener<T, R> {

    public static final String NAME = "ALERT_MISSING_INFO_LISTENNER";
    public static final String SOF_TEAM = "SOF_TEAM";
    public static final String AI_TEAM = "AI_TEAM";
    @Autowire
    private GChatSevice gChatSevice;

    public AlertMissingInfoUserProfileListenner() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        UserProfileInfo userProfileInfo = jobData.getTaskData(SubmitGetUserProfileTask.NAME).getContent();
        if (Utils.isEmpty(getConfig().getServiceAlertNotiMissUserProfile()) || userProfileInfo == null) {
            Log.MAIN.info("[AlertListenner] ::: no have service need alert");
            return;
        }

        if (getConfig().getServiceAlertNotiMissUserProfile().contains(jobData.getServiceId().toLowerCase())) {
            if (!UserProfileInfoValidator.isValid(userProfileInfo)) {
                String message = buildMessageAlert(jobData.getServiceId(), jobData.getInitiatorId(), jobData.getInitiator(), SOF_TEAM);
                gChatSevice.send(jobData.getBase(), message);
            } else {
                if (Utils.isEmpty(userProfileInfo.getIdFrontImageKyc()) || Utils.isEmpty(userProfileInfo.getIdBackImageKyc())) {
                    Log.MAIN.fatal("[AlertListenner] ::: userProfileInfo idFrontImageKyc or is null");
                    String message = buildMessageAlert(jobData.getServiceId(), jobData.getInitiatorId(), jobData.getInitiator(), AI_TEAM);
                    gChatSevice.send(jobData.getBase(), message);
                }
            }
        }
    }

    public String buildMessageAlert(String serviceId, String agentId, String phoneNumber, String user) {
        String content = getConfig().getContentGchat().get(user);
        Log.MAIN.info("content: {}", content);
        return String.format("[Critical] %s - %s - %s - %s", serviceId, agentId, phoneNumber, content);
    }
}
