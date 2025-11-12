package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.crm.CrmConfig;
import com.mservice.fs.onboarding.model.crm.CrmRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.sof.grpc.service.profile.GetUserProfileGrpcTask;
import com.mservice.fs.sof.queue.model.profile.OpenPlatformMsgRequest;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.sof.queue.service.GetUserProfileTask;

public class CrmGetUserProfileTask<T extends CrmRequest, R extends CrmResponse> extends GetUserProfileGrpcTask<PlatformData<T,R>,T,R, OnboardingConfig> {

    public static final TaskName NAME = () -> "GET_USER_PROFILE";

    public CrmGetUserProfileTask() {
        super(NAME);
    }

    @Override
    protected void validateUserProfileInfo(PlatformData<T, R> platformData, TaskData taskData, UserProfileInfo userProfileInfo) throws ValidatorException, BaseException, Exception {
        // NO OOP
    }

    @Override
    protected OpenPlatformMsgRequest.OpenPlatformMsg buildRequest(PlatformData<T, R> platformData, T t) {
        CrmConfig.Config config = platformData.getTaskData(LoadCrmConfigTask.NAME).getContent();
        T crmReq = platformData.getRequest();
        OpenPlatformMsgRequest.OpenPlatformMsg request = new OpenPlatformMsgRequest.OpenPlatformMsg();
        request.setPhone(crmReq.getPhoneNumber());
        request.setCallerId(config.getCallerId());
        request.setPartnerCode(config.getCallerId());
        return request;
    }
}
