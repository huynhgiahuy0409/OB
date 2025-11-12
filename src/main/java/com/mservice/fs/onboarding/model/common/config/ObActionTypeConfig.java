package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.LoanActionType;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.application.confirm.KycResult;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.FaceMatching;
import com.mservice.fs.sof.queue.model.profile.IdType;
import com.mservice.fs.sof.queue.model.profile.Identify;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.DateUtil;
import com.mservice.fs.utils.Utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ObActionTypeConfig {

    CCM {
        @Override
        public OnboardingErrorCode checkKycInitForm(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (!Identify.CONFIRM.equals(userProfileInfo.getIdentify()) && FaceMatching.MATCHED.equals(userProfileInfo.getFaceMatching())) {
                Log.MAIN.info("Kyc confirm != 1 and faceMatching = 1");
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            if (!Identify.CONFIRM.equals(userProfileInfo.getIdentify())) {
                Log.MAIN.info("Kyc confirm != 1 and faceMatching != 1");
                return OnboardingErrorCode.FULL_OCR_FACE_MATCHING;
            }
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.UPDATE_KYC;
            }
            IdType idCardType = userProfileInfo.getIdCardTypeKyc();
            if (!IdType.CCCD.equals(idCardType)) {
                Log.MAIN.info("IdCardType is not CCCD: {}", idCardType);
                return OnboardingErrorCode.UPDATE_KYC;
            }
            String expiredDateString = userProfileInfo.getExpiredDateKyc();
            if (!Constant.KHONG_THOI_HAN.equals(expiredDateString) && DateUtil.toMomoDate(expiredDateString).isBefore(LocalDate.now())) {
                Log.MAIN.info("expiredDate {} is expired ", expiredDateString);
                return OnboardingErrorCode.UPDATE_KYC;
            }
            FaceMatching faceMatching = userProfileInfo.getFaceMatching();
            String pathMainFaceImage = userProfileInfo.getPathMainFaceImage();
            if (Utils.isEmpty(faceMatching) || !FaceMatching.MATCHED.equals(faceMatching)) {
                Log.MAIN.info("FaceMatching [{}] is empty or not MATCHED", faceMatching);
                return OnboardingErrorCode.VERIFY_FACE_MATCHING;
            }
            if (Utils.isEmpty(pathMainFaceImage)) {
                Log.MAIN.info("PathMainFaceImage [{}] is empty", pathMainFaceImage);
                return OnboardingErrorCode.VERIFY_FACE_MATCHING;
            }
            if (ObActionTypeConfig.isNotMapBank(userProfileInfo.getWalletStatus(), userProfileInfo.getIsMapBank())) {
                return OnboardingErrorCode.NOT_MAP_BANK;
            }
            return null;
        }

        @Override
        public OnboardingErrorCode checkKycFinalSubmit(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            return null;
        }

        @Override
        public void handleWhenAddPackageMap(LinkedHashMap<String, PackageInfo> packageInfoMap, PackageInfo packageInfo) {
            if (!PackageStatus.AVAILABLE.name().equals(packageInfo.getPackageStatus())) {
                return;
            }
            PackageInfo packageFromMap = packageInfoMap.get(packageInfo.getPackageCode());
            if (packageFromMap != null && packageInfo.getPackageName().equals(packageFromMap.getPackageName()) && packageFromMap.getLoanAmount() < packageInfo.getLoanAmount()) {
                packageInfoMap.put(packageInfo.getPackageCode(), packageInfo);
                return;
            }
            packageInfoMap.putIfAbsent(packageInfo.getPackageCode(), packageInfo);
        }
    },

    CLO {
        @Override
        public OnboardingErrorCode checkKycInitForm(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (!Identify.CONFIRM.equals(userProfileInfo.getIdentify())) {
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            return null;
        }

        @Override
        public OnboardingErrorCode checkKycFinalSubmit(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (ObActionTypeConfig.isNotMapBank(userProfileInfo.getWalletStatus(), userProfileInfo.getIsMapBank())) {
                return OnboardingErrorCode.NOT_MAP_BANK;
            }
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            return null;
        }

        @Override
        public OnboardingErrorCode checkGetOfferPackageWhenPackageNotFound(GetPackageResponse getPackageResponse, UserProfileInfo userProfileInfo, ApplicationForm applicationForm, OnboardingData<? extends OnboardingRequest, ? extends OnboardingResponse> jobData) {
            return OnboardingErrorCode.PACKAGE_AI_REJECT;
        }
    },

    FAST_MONEY {
        @Override
        public OnboardingErrorCode checkKycInitForm(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (!Identify.CONFIRM.equals(userProfileInfo.getIdentify())) {
                Log.MAIN.info("Kyc confirm != 1");
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.UPDATE_KYC;
            }
            String expiredDateString = userProfileInfo.getExpiredDateKyc();
            if (!OnboardingUtils.isMatchMomoDate(expiredDateString)) {
                Log.MAIN.info("ExpiryDate format is invalid: {} - momoFormat: dd/mm/yyyy", expiredDateString);
                return OnboardingErrorCode.EXPIRED_DATE_KYC;
            }
            if (!Constant.KHONG_THOI_HAN.equals(expiredDateString) && DateUtil.toMomoDate(expiredDateString).isBefore(LocalDate.now())) {
                Log.MAIN.info("expiredDate {} is expired ", expiredDateString);
                return OnboardingErrorCode.EXPIRED_DATE_KYC;
            }
            String dob = userProfileInfo.getDobKyc();
            if (!OnboardingUtils.isMatchMomoDate(dob)) {
                Log.MAIN.info("Dob format is invalid: {} - momoFormat: dd/mm/yyyy", dob);
                return OnboardingErrorCode.UPDATE_KYC;
            }
            int age = Period.between(DateUtil.toMomoDate(dob), LocalDate.now()).getYears();
            if (age < Constant.AGE_18 || age > Constant.AGE_50) {
                return OnboardingErrorCode.DOB_KYC_NOT_IN_18_TO_50;
            }
            if (ObActionTypeConfig.isNotMapBank(userProfileInfo.getWalletStatus(), userProfileInfo.getIsMapBank())) {
                return OnboardingErrorCode.NOT_MAP_BANK;
            }
            return null;
        }

        @Override
        public Integer getResultCodeWithActionLoanDecider(String loanActionName, UserProfileInfo userProfileInfo) {
            Log.MAIN.info("[Check LoanAction FastMoney - GetResultCodeWithActionLoanDecider] userProfileInfo {}", userProfileInfo);

            Log.MAIN.info("ActionName: {}", loanActionName);
            if (Utils.isEmpty(loanActionName)) {
                Log.MAIN.info("ActionName is empty return null");
                return null;
            }

            Log.MAIN.info("User profile user faceMatching :{}", userProfileInfo.getFaceMatching());
            boolean isFaceMatching = userProfileInfo.getFaceMatching().equals(FaceMatching.MATCHED);

            if (LoanActionType.FACE_MATCHING.name().equals(loanActionName) && !isFaceMatching) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_AND_NOT_FACE_MATCHING.getCode();

            } else if (LoanActionType.FACE_MATCHING.name().equals(loanActionName)) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_MATCHING_AND_FACE_MATCHING.getCode();

            } else if (LoanActionType.CHECK_1_N.name().equals(loanActionName) && !isFaceMatching) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), true);
                return OnboardingErrorCode.ACTION_FACE_1_N_AND_NOT_FACE_MATCHING.getCode();

            } else if (LoanActionType.CHECK_1_N.name().equals(loanActionName)) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), true);
                return OnboardingErrorCode.ACTION_FACE_1_N_AND_FACE_MATCHING.getCode();
            }
            return null;
        }

        @Override
        public Integer checkResultCodeFromActionFaceMatching(KycResult kycResult) {
            if (Utils.isEmpty(kycResult) || Utils.isEmpty(kycResult.getFaceData()) || Utils.isEmpty(kycResult.getFaceData().getResultCode())) {
                Log.MAIN.info("KycResult {} return resultCode null", kycResult);
                return null;
            }
            Integer faceResultCode = kycResult.getFaceData().getResultCode();
            Log.MAIN.info("Face resultCode {}");
            if (OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode().equals(faceResultCode)) {
                Log.MAIN.info("Face resultCode {} equals: {}", faceResultCode, OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode());
                return OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode();
            }
            return null;
        }

        @Override
        public void checkPackageFromAI(PackageCache packageCache) throws BaseException {

            if (Utils.isEmpty(packageCache)) {
                Log.MAIN.info("Package is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Log.MAIN.info("Check List Package From AI");
            LinkedHashMap<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();

            if (Utils.isEmpty(packageInfoMap)) {
                Log.MAIN.info("packageInfoMap is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Set<String> availablePackageCodes = new HashSet<>();
            packageInfoMap.forEach((key, value) -> {
                if (PackageStatus.AVAILABLE.name().equals(value.getPackageStatus())) {
                    availablePackageCodes.add(value.getPackageCode());
                }
            });

            if (Utils.isEmpty(availablePackageCodes)) {
                Log.MAIN.info("List package is empty");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
            }
        }

        @Override
        public void handleWhenAddPackageMap(LinkedHashMap<String, PackageInfo> packageInfoMap, PackageInfo packageInfo) {
            if (!PackageStatus.AVAILABLE.name().equals(packageInfo.getPackageStatus())) {
                return;
            }
            packageInfoMap.put(packageInfo.getPackageCode(), packageInfo);
        }
    },

    FMOB {
        @Override
        public OnboardingErrorCode checkKycInitForm(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (!Identify.CONFIRM.equals(userProfileInfo.getIdentify())) {
                Log.MAIN.info("Kyc confirm != 1");
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.UPDATE_KYC;
            }
            String expiredDateString = userProfileInfo.getExpiredDateKyc();
            if (!OnboardingUtils.isMatchMomoDate(expiredDateString)) {
                Log.MAIN.info("ExpiryDate format is invalid: {} - momoFormat: dd/mm/yyyy", expiredDateString);
                return OnboardingErrorCode.EXPIRED_DATE_KYC;
            }
            if (!Constant.KHONG_THOI_HAN.equals(expiredDateString) && DateUtil.toMomoDate(expiredDateString).isBefore(LocalDate.now())) {
                Log.MAIN.info("expiredDate {} is expired ", expiredDateString);
                return OnboardingErrorCode.EXPIRED_DATE_KYC;
            }
            String dob = userProfileInfo.getDobKyc();
            if (!OnboardingUtils.isMatchMomoDate(dob)) {
                Log.MAIN.info("Dob format is invalid: {} - momoFormat: dd/mm/yyyy", dob);
                return OnboardingErrorCode.UPDATE_KYC;
            }
            int age = Period.between(DateUtil.toMomoDate(dob), LocalDate.now()).getYears();
            if (age < Constant.AGE_18 || age > Constant.AGE_50) {
                return OnboardingErrorCode.DOB_KYC_NOT_IN_18_TO_50;
            }
            if (ObActionTypeConfig.isNotMapBank(userProfileInfo.getWalletStatus(), userProfileInfo.getIsMapBank())) {
                return OnboardingErrorCode.NOT_MAP_BANK;
            }
            return null;
        }

        @Override
        public Integer getResultCodeWithActionLoanDecider(String loanActionName, UserProfileInfo userProfileInfo) {
            Log.MAIN.info("[Check LoanAction FastMoney - GetResultCodeWithActionLoanDecider] userProfileInfo {}", userProfileInfo);

            Log.MAIN.info("ActionName: {}", loanActionName);
            if (Utils.isEmpty(loanActionName)) {
                Log.MAIN.info("ActionName is empty return null");
                return null;
            }

            Log.MAIN.info("User profile user faceMatching :{}", userProfileInfo.getFaceMatching());
            boolean isFaceMatching = userProfileInfo.getFaceMatching().equals(FaceMatching.MATCHED);

            if (LoanActionType.FACE_MATCHING.name().equals(loanActionName) && !isFaceMatching) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_AND_NOT_FACE_MATCHING.getCode();

            } else if (LoanActionType.FACE_MATCHING.name().equals(loanActionName)) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_MATCHING_AND_FACE_MATCHING.getCode();

            } else if (LoanActionType.CHECK_1_N.name().equals(loanActionName) && !isFaceMatching) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), true);
                return OnboardingErrorCode.ACTION_FACE_1_N_AND_NOT_FACE_MATCHING.getCode();

            } else if (LoanActionType.CHECK_1_N.name().equals(loanActionName)) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), true);
                return OnboardingErrorCode.ACTION_FACE_1_N_AND_FACE_MATCHING.getCode();
            }
            return null;
        }

        @Override
        public Integer checkResultCodeFromActionFaceMatching(KycResult kycResult) {
            if (Utils.isEmpty(kycResult) || Utils.isEmpty(kycResult.getFaceData()) || Utils.isEmpty(kycResult.getFaceData().getResultCode())) {
                Log.MAIN.info("KycResult {} return resultCode null", kycResult);
                return null;
            }
            Integer faceResultCode = kycResult.getFaceData().getResultCode();
            Log.MAIN.info("Face resultCode {}");
            if (OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode().equals(faceResultCode)) {
                Log.MAIN.info("Face resultCode {} equals: {}", faceResultCode, OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode());
                return OnboardingErrorCode.EXCEEDED_LIMIT_FAIL_FACE_MATCHING.getCode();
            }
            return null;
        }

        @Override
        public void checkPackageFromAI(PackageCache packageCache) throws BaseException {

            if (Utils.isEmpty(packageCache)) {
                Log.MAIN.info("Package is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Log.MAIN.info("Check List Package From AI");
            Map<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();

            if (Utils.isEmpty(packageInfoMap)) {
                Log.MAIN.info("packageInfoMap is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Set<String> availablePackageCodes = new HashSet<>();
            packageInfoMap.forEach((key, value) -> {
                if (PackageStatus.AVAILABLE.name().equals(value.getPackageStatus())) {
                    availablePackageCodes.add(value.getPackageCode());
                }
            });

            if (Utils.isEmpty(availablePackageCodes)) {
                Log.MAIN.info("List package is empty");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
            }
        }
    },

    PAYLATER {
        @Override
        public Integer getResultCodeWithActionLoanDecider(String loanActionName, UserProfileInfo userProfileInfo) {
            Log.MAIN.info("[Check LoanAction FastMoney - GetResultCodeWithActionLoanDecider] userProfileInfo {}", userProfileInfo);

            Log.MAIN.info("ActionName: {}", loanActionName);
            if (Utils.isEmpty(loanActionName)) {
                Log.MAIN.info("ActionName is empty return null");
                return null;
            }

            Log.MAIN.info("User profile user faceMatching :{}", userProfileInfo.getFaceMatching());
            boolean isFaceMatching = userProfileInfo.getFaceMatching().equals(FaceMatching.MATCHED);

            if (LoanActionType.FACE_MATCHING.name().equals(loanActionName) && !isFaceMatching) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_AND_NOT_FACE_MATCHING.getCode();

            } else if (LoanActionType.FACE_MATCHING.name().equals(loanActionName)) {

                Log.MAIN.info("ActionName: {} and isFaceMatching: {}", userProfileInfo.getFaceMatching(), false);
                return OnboardingErrorCode.ACTION_FACE_MATCHING_AND_FACE_MATCHING.getCode();

            }
            return null;
        }

        @Override
        public void checkPackageFromAI(PackageCache packageCache) throws BaseException {

            if (Utils.isEmpty(packageCache)) {
                Log.MAIN.info("Package is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Log.MAIN.info("Check List Package From AI");
            Map<String, PackageInfo> packageInfoMap = packageCache.getPackageInfoMap();

            if (Utils.isEmpty(packageInfoMap)) {
                Log.MAIN.info("packageInfoMap is null");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_EXIST);
            }

            Set<String> availablePackageCodes = new HashSet<>();
            packageInfoMap.forEach((key, value) -> {
                if (PackageStatus.AVAILABLE.name().equals(value.getPackageStatus())) {
                    availablePackageCodes.add(value.getPackageCode());
                }
            });

            if (Utils.isEmpty(availablePackageCodes)) {
                Log.MAIN.info("List package is empty");
                throw new BaseException(OnboardingErrorCode.PACKAGE_NOT_AVAILABLE);
            }
        }

        @Override
        public OnboardingErrorCode checkKycFinalSubmit(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
            if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
                return OnboardingErrorCode.CHECK_KYC_FAIL;
            }
            return null;
        }
    },


    NEWTON {};


    public OnboardingErrorCode checkKycInitForm(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
        Log.MAIN.info("Service do not checkKycInitForm ");
        return null;
    }

    public OnboardingErrorCode checkKycFinalSubmit(ActionInfo actionInfo, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
        if (isEmptyField(actionInfo.getAllowFields(), userProfileInfo)) {
            return OnboardingErrorCode.CHECK_KYC_FAIL;
        }
        return null;
    }

    public OnboardingErrorCode checkKnockOutRuleWhenApprove(KnockOutRuleResponse knockOutRuleResponse, UserProfileInfo userProfileInfo) throws BaseException {
        Log.MAIN.info("Service do not checkKnockOutRuleWhenApprove ");
        return null;
    }

    public OnboardingErrorCode checkGetOfferPackageWhenSuccess(GetPackageResponse getPackageResponse, UserProfileInfo userProfileInfo) {
        Log.MAIN.info("Service do not checkGetOfferPackageWhenSuccess ");
        return null;
    }

    public OnboardingErrorCode checkGetOfferPackageWhenPackageNotFound(GetPackageResponse getPackageResponse, UserProfileInfo userProfileInfo, ApplicationForm applicationForm, OnboardingData<? extends OnboardingRequest, ? extends OnboardingResponse> jobData) {
        return null;
    }

    public void handleWhenAddPackageMap(LinkedHashMap<String, PackageInfo> packageInfoMap, PackageInfo packageInfo) {
        packageInfoMap.put(packageInfo.getPackageCode(), packageInfo);
    }

    public Integer getResultCodeWithActionLoanDecider(String loanActionName, UserProfileInfo userProfileInfo) {
        return null;
    }

    public Integer checkResultCodeFromActionFaceMatching(KycResult kycResult) {
        return null;
    }

    public void checkPackageFromAI(PackageCache packageCache) throws BaseException {
    }

    private static boolean isEmptyField(List<String> fieldNames, UserProfileInfo userProfileInfo) throws NoSuchFieldException, IllegalAccessException {
        if (Utils.isEmpty(fieldNames)) {
            return false;
        }
        for (String fieldName : fieldNames) {
            Field field = UserProfileInfo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (Utils.isEmpty(field.get(userProfileInfo))) {
                Log.MAIN.info("Field {} is empty need to kyc", fieldName);
                return true;
            }
        }
        return false;
    }

    private static boolean isNotMapBank(String walletStatus, Boolean mapBank) {
        if (Utils.isEmpty(mapBank) || Utils.isEmpty(walletStatus) || !(mapBank && Integer.parseInt(walletStatus) >= Constant.NUMBER_MAP_BANK)) {
            Log.MAIN.info("User not map bank mapBank {} walletStatus {}", mapBank, walletStatus);
            return true;
        }
        return false;
    }

    private static Map<String, LoanAction> getLoanActionMap(LoanDeciderRecord loanDeciderRecord) throws BaseException {
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return null;
        }
        return loanActions
                .stream()
                .filter(loanAction -> Utils.isNotEmpty(loanAction.getActionId()))
                .collect(Collectors.toMap(LoanAction::getActionName, Function.identity()));
    }

    private static LoanAction getLoanAction(LoanDeciderRecord loanDeciderRecord, LoanActionType loanActionType) throws BaseException {
        if (Utils.isEmpty(loanActionType)) {
            return null;
        }
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return null;
        }
        for (LoanAction loanAction : loanActions) {
            if (loanActionType.name().equals(loanAction.getActionId())) {
                return loanAction;
            }
        }
        return null;
    }


    private static long convertTimestampStringToLong(String timeStampString) {
        if (Utils.isNotEmpty(timeStampString)) {
            return Long.parseLong(timeStampString);
        }
        return 0;
    }

}
