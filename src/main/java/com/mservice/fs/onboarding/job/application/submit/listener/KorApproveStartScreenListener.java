package com.mservice.fs.onboarding.job.application.submit.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.ai.PackageInfo;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class KorApproveStartScreenListener<T extends OnboardingRequest, R extends OnboardingResponse> extends AbsAIListener<T, R> {

    private static final String NAME = "PUB_KOR_APPROVE_MSG";

    public KorApproveStartScreenListener() {
        super(NAME);
    }
    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    @Override
    protected ByteString createMessageData(OnboardingData<T, R> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        PlutusStartScreenLog build = PlutusStartScreenLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(Utils.nullToEmpty(aiConfig.getLoanProductCode()))
                .setProductId(Utils.nullToEmpty(aiConfig.getProductId()))
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(ScreenId.START_SCREEN)
                .setMomoLoanAppId(Utils.nullToEmpty(applicationData.getApplicationId()))
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setPackageInfo(getPackageInfo(applicationData))
                .setProfile(setProfile(onboardingData, this.getApplicationData(onboardingData)))
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .setSelectedLoanPackage(Utils.isNotEmpty(applicationData.getChosenPackage()) ? Utils.nullToEmpty(applicationData.getChosenPackage().getPackageCode()) : CommonConstant.STRING_EMPTY)
                .build();
        Log.MAIN.info("Start Screen Log: [{}]", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) {
        return LoanInfoMessageType.START_SCREEN_LOG;
    }

    @Override

    protected boolean isValid(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(onboardingData.getServiceId(), onboardingData.getInitiatorId());
        CacheData knockOutRuleCache = cacheStorage.get(knockOutRuleKey);

        KnockOutRuleResponse knockOutRuleResponse = new KnockOutRuleResponse();
        if (knockOutRuleCache != null) {
            knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
        }

        Log.MAIN.info("Knock Out Rule response: [{}]", JsonUtil.toString(knockOutRuleCache));
        return OnboardingErrorCode.SUCCESS.getCode().equals(onboardingData.getResponse().getResultCode()) && Utils.isNotEmpty(knockOutRuleCache) && AIStatus.APPROVE.equals(knockOutRuleResponse.getLoanDeciderRecord().getLoanDeciderStatus());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<T, R> onboardingData) {
        return CommonConstant.STRING_EMPTY;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<T, R> onboardingData) {
        ApplicationForm applicationForm = onboardingData.getTaskData(ApplicationTask.NAME).getContent();
        return applicationForm.getApplicationData();
    }

    @Override
    protected String getPubMessageRequestId(OnboardingData<T, R> onboardingData) throws JsonProcessingException {
        String knockOutRuleKey = KnockOutRuleResponse.createKeyCache(onboardingData.getServiceId(), onboardingData.getInitiatorId());
        CacheData knockOutRuleCache = cacheStorage.get(knockOutRuleKey);

        KnockOutRuleResponse knockOutRuleResponse = new KnockOutRuleResponse();
        if (knockOutRuleCache != null) {
            knockOutRuleResponse = (KnockOutRuleResponse) knockOutRuleCache.getObject();
        }

        Log.MAIN.info("Knock Out Rule response request id: [{}] - [{}]",knockOutRuleResponse.getRequestId(), JsonUtil.toString(knockOutRuleResponse));

        return knockOutRuleResponse.getRequestId();
    }

    private PlutusProfile setProfile(OnboardingData<T, R> onboardingData, ApplicationData applicationData) {
        UserProfileInfo userProfileInfo = onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
        String workingPlace = Utils.isEmpty(applicationData.getCompanyAddress()) ? CommonConstant.STRING_EMPTY : applicationData.getCompanyAddress().getStreet();
        String receivePlace = Utils.isEmpty(applicationData.getShippingAddress()) ? CommonConstant.STRING_EMPTY : applicationData.getShippingAddress().getFullAddress();
        String currentAddress = Utils.isEmpty(applicationData.getCurrentAddress()) ? CommonConstant.STRING_EMPTY : applicationData.getCurrentAddress().getFullAddress();
        long income = Utils.isEmpty(applicationData.getIncome()) ? 0 : applicationData.getIncome();
        return PlutusProfile.newBuilder()
                .setUserName(Utils.nullToEmpty(userProfileInfo.getFullNameKyc()))
                .setUserDob(Utils.nullToEmpty(userProfileInfo.getDobKyc()))
                .setUserEmail(Utils.nullToEmpty(userProfileInfo.getEmail()))
                .setAddress(Utils.nullToEmpty(userProfileInfo.getAddressKyc()))
                .setNationalIdNumber(Utils.nullToEmpty(userProfileInfo.getPersonalIdKyc()))
                .setGender(this.getGender(Utils.nullToEmpty(userProfileInfo.getGenderKyc())))
                .setPhoneNumber(onboardingData.getInitiator())
                .setIssueDate(Utils.nullToEmpty(userProfileInfo.getIssueDateKyc()))
                .setExpireDate(Utils.nullToEmpty(userProfileInfo.getExpiredDateKyc()))
                .setIdCardType(this.getIdCardType(Utils.nullToEmpty(userProfileInfo.getIdCardTypeKyc())))
                .setWorkingPlace(workingPlace)
                .setUserIncome(income)
                .setReceivePlace(receivePlace)
                .setCurrentAddress(Utils.nullToEmpty(currentAddress))
                .addAllRelativesInfo(this.getRelativeProfile(applicationData))
                .build();
    }

    private Gender getGender(String gender) {
        try {
            return Gender.valueOf(gender);
        } catch (Exception ex) {
            return Gender.UNKNOWN_GENDER;
        }
    }

    private IdCardType getIdCardType(String idCardType) {
        try {
            return IdCardType.valueOf(idCardType);
        } catch (Exception ex) {
            return IdCardType.UNKNOWN_ID_CARD_TYPE;
        }
    }

    private List<PlutusRelativeProfile> getRelativeProfile(ApplicationData applicationData) {
        List<PlutusRelativeProfile> relativeProfiles = new ArrayList<>();
        List<ReferencePeople> peopleList = Utils.isEmpty(applicationData.getReferencePeople()) ? new ArrayList<>() : applicationData.getReferencePeople();
        for (ReferencePeople people : peopleList) {
            PlutusRelativeProfile relativeProfile = PlutusRelativeProfile.newBuilder()
                    .setRelativeName(Utils.nullToEmpty(people.getFullName()))
                    .setRelativePhoneNumber(Utils.nullToEmpty(people.getPhoneNumber()))
                    .setRelationship(Utils.nullToEmpty(people.getRelationship().getName())).build();

            relativeProfiles.add(relativeProfile);
        }

        return relativeProfiles;
    }

    private PackageInfo getPackageInfo(ApplicationData applicationData) {
        com.mservice.fs.onboarding.model.PackageInfo chosenPackage = Utils.isEmpty(applicationData.getChosenPackage()) ? new com.mservice.fs.onboarding.model.PackageInfo() : applicationData.getChosenPackage();
        return PackageInfo.newBuilder()
                .setPackageCode(Utils.nullToEmpty(chosenPackage.getPackageCode()))
                .setPackageAmount(chosenPackage.getLoanAmount())
                .build();
    }

}
