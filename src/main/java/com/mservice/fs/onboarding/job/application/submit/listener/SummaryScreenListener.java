package com.mservice.fs.onboarding.job.application.submit.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.CheckLoanDeciderTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ReferencePeople;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SummaryScreenListener extends AbsAIListener<SubmitRequest, SubmitResponse> {

    private static final String NAME = "PUB_SUMMARY_SCREEN_MSG";

    public SummaryScreenListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = super.getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        PlutusSummaryScreenLog build = PlutusSummaryScreenLog.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setLoanProductCode(aiConfig.getLoanProductCode())
                .setProductId(aiConfig.getProductId())
                .setMiniAppVersion(Utils.nullToEmpty(onboardingData.getBase().getHeaders().get(Constant.MINI_APP_TRACK_VER)))
                .setAgentId(Utils.isNotEmpty(onboardingData.getInitiatorId()) ? Long.parseLong(onboardingData.getInitiatorId()) : 0)
                .setScreenId(ScreenId.SUMMARY_SCREEN)
                .setMomoLoanAppId(applicationData.getApplicationId())
                .setProfile(getProfile(onboardingData, this.getApplicationData(onboardingData)))
                .setSelectedLoanPackage(Utils.isNotEmpty(applicationData.getChosenPackage()) ? Utils.nullToEmpty(applicationData.getChosenPackage().getPackageCode()) : CommonConstant.STRING_EMPTY)
                .setMomoCreditScore(Utils.isNotEmpty(applicationData.getCurrentCreditScore()) ? applicationData.getCurrentCreditScore() : 0.0)
                .setUserGroup(this.getGroup(applicationData))
                .setOrderInfo(OrderInfo.newBuilder().build())
                .setLendingFlow(LendingFlow.LENDING_FLOW_REGISTRATION)
                .setLoanAppStatus(LoanAppStatus.LOAN_STATUS_IN_PROGRESS)
                .setCashLoanItem(this.getCashLoanItem(applicationData))
                .build();
        Log.MAIN.info("Summary Screen Log: {}", OnboardingUtils.encode(build));
        return build.toByteString();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        return LoanInfoMessageType.SUMMARY_SCREEN_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        LoanDeciderResponse loanDeciderResponse = onboardingData.getTaskData(CheckLoanDeciderTask.NAME).getContent();
        Log.MAIN.info("Loan decider data: [{}]", JsonUtil.toString(loanDeciderResponse));
        return Utils.isNotEmpty(loanDeciderResponse);
    }

    @Override
    protected String getPartnerResponse(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        return null;
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        ApplicationForm applicationForm = onboardingData.getTaskData(ApplicationTask.NAME).getContent();
        return applicationForm.getApplicationData();
    }

    @Override
    protected String getPubMessageRequestId(OnboardingData<SubmitRequest, SubmitResponse> onboardingData) {
        return onboardingData.getRequest().getRequestId();
    }

    private PlutusProfile getProfile(OnboardingData<SubmitRequest, SubmitResponse> onboardingData, ApplicationData applicationData) {
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
                    .setRelativeName(people.getFullName())
                    .setRelativePhoneNumber(people.getPhoneNumber())
                    .setRelationship(people.getRelationship().getName()).build();

            relativeProfiles.add(relativeProfile);
        }

        return relativeProfiles;
    }

    private UserGroup getGroup(ApplicationData applicationData) {
        if (Utils.isEmpty(applicationData.getIdNumber())
                || Utils.isEmpty(applicationData.getDob())
                || Utils.isEmpty(applicationData.getFullName())
                || Utils.isEmpty(applicationData.getIssueDate())
                || Utils.isEmpty(applicationData.getExpiryDate())
                || Utils.isEmpty(applicationData.getPermanentAddress().getFullAddress())
                || Utils.isEmpty(applicationData.getFrontPersonalIdImage())
                || Utils.isEmpty(applicationData.getBackPersonalIdImage())) {
            return UserGroup.GROUP_B;
        }
        return UserGroup.GROUP_A;
    }

    private CashLoanItem getCashLoanItem(ApplicationData applicationData) {
        if (applicationData.getChosenPackage() == null) {
            return CashLoanItem.newBuilder().build();
        }
        return CashLoanItem.newBuilder()
                .setLoanAmount(applicationData.getChosenPackage().getLoanAmount())
                .build();
    }
}
