package com.mservice.fs.onboarding.job.updatestatus.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.AbsAIListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ai.LoanInfoMessageType;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.notipackage.protomodel.GetOfferPackageResponse;
import com.mservice.fs.onboarding.model.notipackage.protomodel.Lender;
import com.mservice.fs.onboarding.model.notipackage.protomodel.LoanProductCode;
import com.mservice.fs.onboarding.model.notipackage.protomodel.OfferPackage;
import com.mservice.fs.onboarding.model.notipackage.protomodel.ProductGroup;
import com.mservice.fs.onboarding.model.notipackage.protomodel.SegmentUser;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 5/6/2025
 **/
public class PushOfferPackageAIListener extends AbsAIListener<UpdatingStatusRequest, UpdatingStatusResponse> {
    private static final String NAME = "PUB_OFFER_PACKAGE_MSG";

    public PushOfferPackageAIListener() {
        super(NAME);
    }

    @Override
    protected ByteString createMessageData(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData, ApplicationData applicationData, long agentId) throws BaseException, ValidatorException, Exception {
        AiConfig aiConfig = getServiceAIInfo(onboardingData.getServiceId()).getAiConfig();
        UpdatingStatusRequest request = onboardingData.getRequest();
        Lender lenderId = Utils.isEmpty(applicationData.getChosenPackage()) || Utils.isEmpty(applicationData.getChosenPackage().getLenderId()) ? Lender.UNKNOWN_LENDER : Lender.valueOf(applicationData.getChosenPackage().getLenderId());
        GetOfferPackageResponse offerPackageResponse = GetOfferPackageResponse.newBuilder()
                .setRequestId(onboardingData.getRequestId())
                .setAgentId(agentId)
                .setLoanProductCode(LoanProductCode.valueOf(aiConfig.getLoanProductCode()))
                .setProductGroup(ProductGroup.valueOf(aiConfig.getProductGroup()))
                .setResponseMessage(request.getReasonMessage())
                .setResponseTimestamp(System.currentTimeMillis())
                .setLenderId(lenderId)
                .setSegmentUser(Utils.isNotEmpty(request.getSegmentUser()) ? SegmentUser.valueOf(request.getSegmentUser().name()) : SegmentUser.valueOf(request.getChosenPackage().getSegmentUser()))
                .setResponseCode(onboardingData.getResponse().getResultCode())
                .setMomoLoanAppId(applicationData.getApplicationId())
                .addOfferPackages(buildOfferPackage(onboardingData, applicationData, lenderId))
                .build();
        Log.MAIN.info("MessageData to AI for type [{}] || [{}]", getLoanMessageType(onboardingData), OnboardingUtils.encode(offerPackageResponse));
        return offerPackageResponse.toByteString();
    }

    private OfferPackage buildOfferPackage(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData, ApplicationData applicationData, Lender lenderId) {
        Log.MAIN.info("Application Data: [{}]", Json.encode(applicationData));
        UpdatingStatusRequest request = onboardingData.getRequest();
        PackageInfo packageInfo = Utils.coalesce(applicationData.getChosenPackage(), request.getChosenPackage());
        PackageInfo packageInfoRequest = request.getChosenPackage();
        return OfferPackage.newBuilder()
                .setPackageGroup(Utils.nullToEmpty(packageInfo.getPackageGroup()))
                .setPackageName(Utils.nullToEmpty(packageInfo.getPackageName()))
                .setPackageCode(Utils.coalesce(packageInfoRequest.getPackageCode(), request.getOfferId(), packageInfo.getPackageCode()))
                .setLenderId(lenderId)
                .setRank(Utils.coalesce(packageInfo.getRank(), 0))
                .setPackageAmount(Utils.coalesce(packageInfoRequest.getLoanAmount(), packageInfo.getLoanAmount(), 0L).intValue())
                .setPackageStatus(Utils.nullToEmpty(packageInfo.getPackageStatus()))
                .setPackageType(Utils.nullToEmpty(packageInfo.getOfferPackageType()))
                .setTenor(packageInfo.getTenor())
                .setInterestRate(packageInfo.getInterest())
                .setInterestAmount(packageInfo.getInterestAmount())
                .setDisbursedAmount(packageInfo.getDisbursedAmount())
                .setPaymentAmount(packageInfo.getPaymentAmount())
                .setMinLoanAmount(Utils.coalesce(packageInfoRequest.getMinLoanAmount(), packageInfo.getMinLoanAmount(), 0L).intValue())
                .setMaxLoanAmount(Utils.coalesce(packageInfoRequest.getMaxLoanAmount(), packageInfo.getMaxLoanAmount(), 0L).intValue())
                .setServiceFee(packageInfo.getServiceFee())
                .setCollectionFee(packageInfo.getCollectionFee())
                .setDisbursedFee(packageInfo.getDisbursedFee())
                .setLateInterestRate(packageInfo.getLateInterest())
                .setLateFee(packageInfo.getLateFee())
                .setEmi(packageInfo.getEmi())
                .build();
    }

    @Override
    protected LoanInfoMessageType getLoanMessageType(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) throws BaseException, ValidatorException, Exception {
        return LoanInfoMessageType.OFFER_PACKAGE_RESULT_LOG;
    }

    @Override
    protected boolean isValid(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData, ServiceObInfo serviceObInfo) throws JsonProcessingException {
        UpdatingStatusRequest request = onboardingData.getRequest();
        return onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode()) &&
                request.isSyncAI() &&
                (Utils.isNotEmpty(request.getSegmentUser()) || Utils.isNotEmpty(request.getChosenPackage().getSegmentUser()));
    }

    @Override
    protected String getPartnerResponse(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
        return onboardingData.getRequest().getRawPartnerRequest();
    }

    @Override
    protected ApplicationData getApplicationData(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> onboardingData) {
        return onboardingData.getTaskData(UpdatingStatusTask.NAME).getContent();
    }
}
