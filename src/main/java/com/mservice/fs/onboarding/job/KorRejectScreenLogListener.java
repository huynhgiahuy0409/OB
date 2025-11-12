package com.mservice.fs.onboarding.job;

import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

public class KorRejectScreenLogListener<T extends OnboardingRequest, R extends OnboardingResponse> extends AbsAIListener<T, R> {

    private static final String NAME = "PUB_KOR_REJECT_MSG";

    public KorRejectScreenLogListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<T, R> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        ScreenId screenId = getLoanMessageType(onboardingData) == LoanInfoMessageType.START_SCREEN_LOG ? ScreenId.START_SCREEN : ScreenId.MID_SCREEN;
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        PlutusStartScreenLog build = PlutusStartScreenLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(aiConfig.getLoanProductCode())
                .setProductId(aiConfig.getProductId())
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(screenId)
                .setMomoLoanAppId(Utils.nullToEmpty(applicationData.getApplicationId()))
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setPackageInfo(PackageInfo.newBuilder().build())
                .setProfile(setProfile(onboardingData))
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .setSelectedLoanPackage(Utils.isNotEmpty(applicationData.getChosenPackage()) ? Utils.nullToEmpty(applicationData.getChosenPackage().getPackageCode()) : CommonConstant.STRING_EMPTY)
                .build();
        Log.MAIN.info("Start Screen Log: {}", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<T, R> onboardingData) throws BaseException, ValidatorException, Exception {
        return LoanInfoMessageType.START_SCREEN_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<T, R> onboardingData, ServiceObInfo serviceObInfo) {
        return OnboardingErrorCode.KNOCK_OUT_RULE_REJECT.getCode().equals(onboardingData.getResponse().getResultCode());
    }

    @Override
    protected String getPartnerResponse(OnboardingData<T, R> onboardingData) {
        return CommonConstant.STRING_EMPTY;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<T, R> onboardingData) {
        return new ApplicationData();
    }

    private PlutusProfile setProfile(OnboardingData<T, R> onboardingData) {
        UserProfileInfo userProfileInfo = onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
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
                .setWorkingPlace(CommonConstant.STRING_EMPTY)
                .setReceivePlace(CommonConstant.STRING_EMPTY)
                .setCurrentAddress(CommonConstant.STRING_EMPTY)
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
}
