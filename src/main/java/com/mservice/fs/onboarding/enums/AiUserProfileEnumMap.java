package com.mservice.fs.onboarding.enums;

import com.mservice.fs.onboarding.model.ai.FaceMatchingStatus;
import com.mservice.fs.onboarding.model.ai.KycConfirmStatus;
import com.mservice.fs.onboarding.model.common.ai.SummaryScreenLog;
import com.mservice.fs.sof.queue.model.profile.FaceMatching;
import com.mservice.fs.sof.queue.model.profile.Gender;
import com.mservice.fs.sof.queue.model.profile.IdCardTypeDetailKyc;
import com.mservice.fs.sof.queue.model.profile.Identify;

import java.util.EnumMap;

/**
 * @author thang.nguyen15
 */
public class AiUserProfileEnumMap {
    public static EnumMap<Identify, KycConfirmStatus> kycStatusMap;
    public static EnumMap<FaceMatching, FaceMatchingStatus> faceStatusMap;
    public static EnumMap<IdCardTypeDetailKyc, SummaryScreenLog.IdCardType> idCardTypeMap;
    public static EnumMap<Gender, SummaryScreenLog.Gender> genderMap;

    static {
        kycStatusMap = new EnumMap<>(Identify.class);
        kycStatusMap.put(Identify.UNCONFIRM, KycConfirmStatus.KYC_UNCONFIRM);
        kycStatusMap.put(Identify.CONFIRM, KycConfirmStatus.KYC_CONFIRM);
        kycStatusMap.put(Identify.WAITING, KycConfirmStatus.KYC_WAITING_APPROVE);
        kycStatusMap.put(Identify.WAITING_UPDATE, KycConfirmStatus.KYC_WAITING_UPDATE);
        kycStatusMap.put(Identify.PROVIDEDINFO, KycConfirmStatus.KYC_PROVIDED_INFO);

        faceStatusMap = new EnumMap<>(FaceMatching.class);
        faceStatusMap.put(FaceMatching.STILL_NOT_MATCHING, FaceMatchingStatus.FM_STILL_NOT_MATCHING);
        faceStatusMap.put(FaceMatching.NOT_MATCHED, FaceMatchingStatus.FM_NOT_MATCHED);
        faceStatusMap.put(FaceMatching.MATCHED, FaceMatchingStatus.FM_MATCHED);
        faceStatusMap.put(FaceMatching.WAITING_APPROVE, FaceMatchingStatus.FM_WAITING_APPROVE);
        faceStatusMap.put(FaceMatching.WAITING_UPDATE, FaceMatchingStatus.FM_WAITING_UPDATE);

        idCardTypeMap = new EnumMap<>(IdCardTypeDetailKyc.class);
        idCardTypeMap.put(IdCardTypeDetailKyc.PASSPORT, SummaryScreenLog.IdCardType.PASSPORT);
        idCardTypeMap.put(IdCardTypeDetailKyc.CMND9, SummaryScreenLog.IdCardType.CMND);
        idCardTypeMap.put(IdCardTypeDetailKyc.CMND12, SummaryScreenLog.IdCardType.CMND);
        idCardTypeMap.put(IdCardTypeDetailKyc.CCCD, SummaryScreenLog.IdCardType.CCCD);
        idCardTypeMap.put(IdCardTypeDetailKyc.CHIP, SummaryScreenLog.IdCardType.CHIP);

        genderMap = new EnumMap<>(Gender.class);
        genderMap.put(Gender.MALE, SummaryScreenLog.Gender.MALE);
        genderMap.put(Gender.FEMALE, SummaryScreenLog.Gender.FEMALE);
    }
}
