package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ActionInfo;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.grpc.service.profile.GetUserProfileGrpcTask;
import com.mservice.fs.sof.queue.model.profile.OpenPlatformMsgRequest;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author muoi.nong
 */
public class AbsGetUserProfileTask<T extends OnboardingRequest, R extends OnboardingResponse> extends GetUserProfileGrpcTask<OnboardingData<T, R>, T, R, OnboardingConfig> {
    public static final TaskName NAME = () -> "GET_USER_PROFILE";

    @Autowire(name = "ServiceConfigInfo")
    protected DataService<ServiceObConfig> onboardingDataInfo;

    public AbsGetUserProfileTask() {
        super(NAME);
    }

    @Override
    protected OpenPlatformMsgRequest.OpenPlatformMsg buildRequest(OnboardingData<T, R> jobData, T t) {
        OpenPlatformMsgRequest.OpenPlatformMsg momoMsg = new OpenPlatformMsgRequest.OpenPlatformMsg();
        try {
            String callerId = Utils.isNotEmpty(onboardingDataInfo.getData().getCallerId(jobData.getServiceId(), jobData.getPartnerId())) ? onboardingDataInfo.getData().getCallerId(jobData.getServiceId(), jobData.getPartnerId()) : getConfig().getOnboardingService();
            momoMsg.setCallerId(callerId);
            momoMsg.setPartnerCode(callerId); //SOF commit callerId = partnerCode
            momoMsg.setAgentId(jobData.getInitiatorId());
        } catch (BaseException | Exception | ValidatorException e) {
            Log.MAIN.error("Error getting service info ", e);
            throw new RuntimeException(e);
        }
        return momoMsg;
    }

    @Override
    protected void validateUserProfileInfo(OnboardingData jobData, TaskData taskData, UserProfileInfo userProfileInfo) throws ValidatorException, BaseException, Exception {
        Log.MAIN.info("Do logic with userProfileInfo with FullNameKyc: {} and FullNameKycOcr: {}", userProfileInfo.getFullNameKyc(), userProfileInfo.getFullNameKycOcr());
        userProfileInfo.setFullNameKyc(userProfileInfo.getFullNameKycOcr());
        try {
            String serviceId = jobData.getServiceId();
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(serviceId);
            Action actionCheckUserProfile = Action.CHECK_USER_PROFILE;
            if (!serviceObInfo.isMatchAction(actionCheckUserProfile, jobData.getProcessName())) {
                Log.MAIN.info("ServiceId {} does not need to check Userprofile, by pass...", serviceId);
            } else {
                ActionInfo actionInfo = serviceObInfo.getActionInfo(actionCheckUserProfile, jobData.getProcessName());
                checkUserProfileWithAction(userProfileInfo, jobData, serviceObInfo, actionInfo);
            }
        } catch (ValidatorException e) {
            Log.MAIN.error("Get ServiceObInfo fail with serviceId {}", jobData.getServiceId());
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
    }

    protected void checkUserProfileWithAction(UserProfileInfo userProfileInfo, OnboardingData<T, R> jobData, ServiceObInfo serviceObInfo, ActionInfo actionCheckUserProfile) throws Exception, BaseException, ValidatorException {

    }
}
