package com.mservice.fs.onboarding.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.mservice.fs.app.component.RenderData;
import com.mservice.fs.app.component.cta.CtaFeature;
import com.mservice.fs.base.PlatformData;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.json.Json;
import com.mservice.fs.json.JsonObject;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.*;
import com.mservice.fs.onboarding.enums.FaceDataResult;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.enums.UserProfileConfigValue;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.confirm.FaceData;
import com.mservice.fs.onboarding.model.application.submit.ScamAlertResult;
import com.mservice.fs.onboarding.model.common.ai.ActionStatus;
import com.mservice.fs.onboarding.model.common.ai.ActionType;
import com.mservice.fs.onboarding.model.common.ai.PackageStatus;
import com.mservice.fs.onboarding.model.common.ai.UserActionEvent;
import com.mservice.fs.onboarding.model.common.config.*;
import com.mservice.fs.onboarding.model.status.PackageCache;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractResponse;
import com.mservice.fs.onboarding.model.verifyotp.UnLockOtpDate;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.sof.queue.model.profile.FaceMatching;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.*;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author hoang.thai
 * on 10/30/2023
 */
public class OnboardingUtils {

    private static final String DATE_UNLOCK_OTP = "dateUnlockOtp";

    public static void mapApplicationInfoToApplicationData(ApplicationInfo applicationInfoNew, ApplicationData applicationData) throws Exception {
        validateApplicationInfo(applicationInfoNew, applicationData);
        Map<String, Field> mapFields = Generics.getMapAllFields(applicationInfoNew.getClass());
        Generics.scanFields(ApplicationData.class, field -> {
                    Field infoField = mapFields.get(field.getName());
                    if (infoField != null) {
                        field.setAccessible(true);
                        infoField.setAccessible(true);
                        Object valueInfoField = infoField.get(applicationInfoNew);
                        if (Utils.isNotEmpty(valueInfoField)) {
                            if (Map.class.isAssignableFrom(infoField.getType())) {
                                updateApplicationAdditionalData(applicationInfoNew.getApplicationAdditionalData(), applicationData, field);
                            } else {
                                field.set(applicationData, valueInfoField);
                            }
                        }
                    }
                }
        );
    }

    private static void updateApplicationAdditionalData(Map<String, Object> additionalDataNew, ApplicationData applicationData, Field field) throws IllegalAccessException {
        Log.MAIN.info("updateApplicationAdditionalData");
        String fieldName = field.getName();
        if (!"applicationAdditionalData".equals(fieldName)) {
            Log.MAIN.info("Field intanceOf Map but not applicationAdditionalData");
            return;
        }
        Map<String, Object> valueInfo = Utils.isEmpty(field.get(applicationData)) ? new HashMap<>() : (Map<String, Object>) field.get(applicationData);
        for (String key : additionalDataNew.keySet()) {
            Object valueNew = additionalDataNew.get(key);
            if (Utils.isNotEmpty(valueNew)) {
                valueInfo.put(key, valueNew);
            }
        }
        field.set(applicationData, valueInfo);
    }

    private static void validateApplicationInfo(ApplicationInfo applicationInfo, ApplicationData applicationData) throws Exception {
        Log.MAIN.info("applicationId: {}, agentId: {}, Validate applicationInfo: {}", applicationData.getApplicationId(), applicationData.getAgentId(), applicationInfo);
        applicationInfo.setCurrentAddress(validateAddress(applicationInfo.getCurrentAddress()));
        applicationInfo.setCompanyAddress(validateAddress(applicationInfo.getCompanyAddress()));
        applicationInfo.setShippingAddress(validateAddress(applicationInfo.getShippingAddress()));
        applicationInfo.setPermanentAddress(validateAddress(applicationInfo.getPermanentAddress()));
        applicationInfo.setPlaceOfBirth(validateAddress(applicationInfo.getPlaceOfBirth()));
    }

    private static Address validateAddress(Address address) {
        if (Utils.isEmpty(address)) {
            return null;
        }

        if (Utils.isEmpty(address.getFullAddress())
                && Utils.isEmpty(address.getStreet())
                && (Utils.isEmpty(address.getWard()) || Utils.isEmpty(address.getWard().getName()))
                && (Utils.isEmpty(address.getDistrict()) || Utils.isEmpty(address.getDistrict().getName()))
                && (Utils.isEmpty(address.getProvince()) || Utils.isEmpty(address.getProvince().getName()))
        ) {
            // set address null to ApplicationInfo
            // for not update to ApplicationData when call mapApplicationInfoToApplicationData method
            Log.MAIN.info("Set address of applicationInfo from {} -> null", address);
            return null;
        }
        return address;
    }

    public static void main(String[] args) throws IOException {
        ApplicationInfo applicationInfo = JsonUtil.fromString("", ApplicationInfo.class);
        ApplicationData applicationData = JsonUtil.fromString("{\"fullName\":\"Đặng Ngọc Lịch\",\"serviceId\":\"finance_creditcard_marketplace\",\"partnerId\":\"ccm_vcb\",\"applicationId\":\"VCB16261929633\",\"createdDate\":1721019255465,\"expiredTimeInMillis\":1721624055465,\"chosenPackage\":{\"packageName\":\"VCB_VISA_MOMO\",\"packageCode\":\"VCB010\",\"rank\":1,\"packageStatus\":\"AVAILABLE\",\"productGroup\":\"CCM\",\"tenor\":0,\"loanAmount\":30000000,\"disbursedAmount\":0,\"interestAmount\":0,\"interestUnit\":\"YEAR\",\"serviceFee\":0,\"collectionFee\":0,\"disbursedFee\":0,\"lateInterest\":0,\"lateFee\":0,\"interest\":17,\"monthlyInterestRate\":\"0,0%\",\"paymentAmount\":0,\"emi\":0,\"tenorUnit\":\"DAY\",\"segmentUser\":\"NEW\",\"partnerId\":\"ccm_vcb\",\"packageMapName\":\"VCB VISA MOMO\",\"lenderName\":\"Vietcombank\",\"minLoanAmount\":0,\"maxLoanAmount\":0},\"status\":\"ACCEPTED_BY_MOMO\",\"state\":\"PENDING\",\"active\":false,\"modifiedDateInMillis\":1721025807172,\"otpInfo\":{\"currentTimesGenerate\":0,\"currentTimesVerify\":0,\"maxVerifyTimes\":3,\"maxGenerateTimes\":3,\"validOtpInMillis\":60000,\"lastModifiedTimeInMillis\":1721019684932,\"lastTimeGenerateOtpInMillis\":0,\"unlockOtpTimeInMillis\":0},\"reasonId\":0,\"frontPersonalIdImage\":{\"path\":\"TEST/2022/7/21/0911632641_FRONT_555.jpg\",\"url\":\"https://s3.ap-southeast-1.amazonaws.com/ekyc-dev.mservice.io/TEST/2022/7/21/0911632641_FRONT_555.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240715T064326Z&X-Amz-SignedHeaders=host&X-Amz-Expires=900&X-Amz-Credential=AKIAQBBQAO45LRIGNSNJ%2F20240715%2Fap-southeast-1%2Fs3%2Faws4_request&X-Amz-Signature=8bb420cc4ffd0ef00091c77b5df697a42fee93513d68d4b9e8288840db666b3e\"},\"backPersonalIdImage\":{\"path\":\"TEST/2022/7/21/0911632641_BACK_379.jpg\",\"url\":\"https://s3.ap-southeast-1.amazonaws.com/ekyc-dev.mservice.io/TEST/2022/7/21/0911632641_BACK_379.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240715T064326Z&X-Amz-SignedHeaders=host&X-Amz-Expires=900&X-Amz-Credential=AKIAQBBQAO45LRIGNSNJ%2F20240715%2Fap-southeast-1%2Fs3%2Faws4_request&X-Amz-Signature=2d43b34da4db735b5751c6bd9cf8a2b0f7658e691ea8b69fc19df22a4d58f902\"},\"faceMatchingImage\":{\"path\":\"TEST/2024/3/13/0938522611_MAIN_FACE_135555.jpg\",\"url\":\"https://s3.ap-southeast-1.amazonaws.com/ekyc-dev.mservice.io/TEST/2024/3/13/0938522611_MAIN_FACE_135555.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240715T064326Z&X-Amz-SignedHeaders=host&X-Amz-Expires=900&X-Amz-Credential=AKIAQBBQAO45LRIGNSNJ%2F20240715%2Fap-southeast-1%2Fs3%2Faws4_request&X-Amz-Signature=3d42a8a08c4ad6151210dbc61aeb1633c2dac781d941345dcaa42720f65f5006\"},\"faceIdCardImage\":{\"url\":\"https://s3.ap-southeast-1.amazonaws.com/ekyc-dev.mservice.io/TEST/2024/7/15/41479259_MAIN_FACE_ID_115327.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240715T064319Z&X-Amz-SignedHeaders=host&X-Amz-Expires=900&X-Amz-Credential=AKIAQBBQAO45LRIGNSNJ%2F20240715%2Fap-southeast-1%2Fs3%2Faws4_request&X-Amz-Signature=63e5fcc1b2849488315ff6af8f9eb18262dc4b636ae2ba4a3ce554da231469c3\"},\"dob\":\"11/11/1999\",\"idNumber\":\"066099015822\",\"expiryDate\":\"11/11/2024\",\"issueDate\":\"12/08/2021\",\"issuePlace\":\"CỤC TRƯỞNG CỤC CẢNH SÁT QUẢN LÝ HÀNH CHÍNH VỀ TRẬT TỰ XÃ HỘI\",\"nationality\":\"Việt Nam\",\"phoneNumber\":\"0911632641\",\"initiator\":\"0911632641\",\"idType\":\"CCCD\",\"email\":\"an@gmail.com\",\"gender\":\"MALE\",\"income\":161616659,\"agentId\":\"41479259\",\"placeOfBirth\":{\"ward\":{\"id\":-1,\"name\":\"\"},\"district\":{\"id\":-1,\"name\":\"\"},\"province\":{\"id\":87,\"name\":\"Tỉnh Đồng Tháp\"},\"street\":\"\",\"fullAddress\":\"\"},\"companyAddress\":{\"ward\":{\"id\":-1,\"name\":\"\"},\"district\":{\"id\":-1,\"name\":\"\"},\"province\":{\"id\":-1,\"name\":\"\"},\"street\":\"\",\"fullAddress\":\"\"},\"currentAddress\":{\"ward\":{\"id\":3673,\"name\":\"Xã Chiềng Ngần\"},\"district\":{\"id\":116,\"name\":\"Thành phố Sơn La\"},\"province\":{\"id\":14,\"name\":\"Tỉnh Sơn La\"},\"street\":\"hrhehe\",\"fullAddress\":\"\"},\"shippingAddress\":{\"ward\":{\"id\":-1,\"name\":\"\"},\"district\":{\"id\":-1,\"name\":\"\"},\"province\":{\"id\":-1,\"name\":\"\"},\"street\":\"\",\"fullAddress\":\"SỐ NHÀ 54/103 PHẠM HỒNG THÁI, TỰ AN, BUÔN MA THUỘT, ĐẮK LẮK\"},\"permanentAddress\":{\"ward\":{\"id\":-1,\"name\":\"\"},\"district\":{\"id\":-1,\"name\":\"\"},\"province\":{\"id\":-1,\"name\":\"\"},\"street\":\"\",\"fullAddress\":\"SỐ NHÀ 54/103 PHẠM HỒNG THÁI, TỰ AN, BUÔN MA THUỘT, ĐẮK LẮK\"},\"referencePeople\":[{\"phoneNumber\":\"0822626658\",\"fullName\":\"ehheje\",\"relationship\":{\"id\":9,\"name\":\"Người thân khác\"}},{\"phoneNumber\":\"0983551861\",\"fullName\":\"ruuee\",\"relationship\":{\"id\":9,\"name\":\"Người thân khác\"}}],\"applicationAdditionalData\":{\"cif\":\"0\",\"jobInfo\":{\"companyName\":\"\",\"incomeType\":{\"id\":-1,\"name\":\"\"},\"occupation\":{\"id\":-1,\"name\":\"\",\"incomeType\":false},\"position\":{\"id\":-1,\"name\":\"\"},\"startWorkingDate\":\"\",\"workingStatus\":{\"id\":-1,\"name\":\"\",\"isStillWorking\":false}},\"deviceInfo\":{\"deviceId\":\"edd6ec210445021392c94ea061c5575caa110fc36f158c14c7d84c6e9d22f597\",\"location\":\"momo\",\"ipDevice\":\"\"},\"maritalStatus\":{\"id\":-1,\"name\":\"\"},\"creditCardInfo\":{\"acceptTNC\":true,\"acceptActive\":true,\"cardInsurance\":false,\"fatcaStatus\":{\"legalAgreement\":false,\"politicalInfluencers\":false,\"residentStatus\":true,\"signalUSA\":false,\"isCheckFatca\":true},\"checkboxCurrentAddress\":false,\"takeCardOption\":{\"id\":1,\"name\":\"Nơi ở hiện tạiiiii\",\"haveAddressPicker\":false},\"issueType\":true,\"securityAnswer\":\"jdjdjdj\",\"hasCreditCard\":false,\"healthInsuranceId\":\"\",\"numberOfCards\":656565665}},\"currentCreditScore\":800,\"loanDeciderData\":{\"profile\":{\"nationalIdNumber\":\"066099015822\",\"nationalIdNumberTwo\":\"241836993\",\"incomeInfoStatus\":\"NOT_HAS_INCOME_INFO\"},\"requiredActionLoanDecider\":\"FACE_MATCHING\"},\"oldIdNumber\":[]}", ApplicationData.class);
    }

    public static String formatDateAI(String date) {
        if (Utils.isEmpty(date)) {
            Log.MAIN.info("formatDateAI: date is empty");
            return CommonConstant.STRING_EMPTY;
        }
        return date.replace('/', '-');
    }

    public static String encode(MessageOrBuilder object) throws InvalidProtocolBufferException {
        return new JsonObject(JsonFormat.printer().print(object)).encode();
    }

    public static <D extends OnboardingResponse> D createAiRuleResponse(Class<?> responseClass, LoanActionAiConfig loanActionAiConfig, OnboardingData<?, ?> jobData, ApplicationForm applicationForm) throws Exception {
        D response = (D) Generics.createObject(responseClass);

        response.setResultCode(loanActionAiConfig.getResultCode());
        response.setResultMessage(loanActionAiConfig.getResultMessage());
        jobData.putProcessNameToTemPlateModel(loanActionAiConfig.getRedirectProcessName());
        return response;
    }

    public static boolean isMatchMomoDate(String date) {
        try {
            DateUtil.toMomoDate(date);
            return true;
        } catch (Exception e) {
            Log.MAIN.info("Wrong format date kyc");
            return false;
        }
    }

    public static String createPDFBased64(String htmlSource, String type, String partnerId) throws BaseException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlSource, null);
            builder.useFastMode();
            builder.toStream(os);
            builder.run();
            return Utils.encodeBase64(os.toByteArray());
        } catch (IOException e) {
            Log.MAIN.error("Error when create base64 partnerId {} type {}", partnerId, type, e);
            throw new BaseException(OnboardingErrorCode.GENERATE_CONTRACT_FAIL);
        }
    }

    public static void handleAttachFileData(List<QueueContractResponse.AttachFileData> attachFiles, BiConsumer<QueueContractResponse.AttachFileData, ContractType> handler) throws Exception {
        for (QueueContractResponse.AttachFileData attachFileData : attachFiles) {
            String[] nameSplit = attachFileData.getName().split(Constant.STRING_JOIN_NAME_AND_TYPE_CONTRACT);
            String fileNameExt = nameSplit[nameSplit.length - 1];
            if (handler != null) {
                handler.accept(attachFileData, ContractType.valueOf(fileNameExt));
            }
        }
    }

    public static <T extends PlatformData> void addCacheForNotify(T onboardingData, Long timeRemindInMillis, ApplicationListWrapper applicationListWrapper, RedisCacheStorage cacheStorage) {
        if (Utils.isEmpty(applicationListWrapper) ||
                Utils.isEmpty(applicationListWrapper.getApplicationForms()) ||
                Utils.isEmpty(applicationListWrapper.getApplicationForms().getFirst().getApplicationData())) {
            Log.MAIN.info("Data is empty - skip cache for notify");
            return;
        }
        CacheData notifyUserCache = getCacheData(onboardingData, timeRemindInMillis, applicationListWrapper);
        String applicationId = applicationListWrapper.getApplicationForms().getFirst().getApplicationData().getApplicationId();
        cacheStorage.put(ApplicationListWrapper.createKeyNotify(onboardingData.getServiceId(), onboardingData.getInitiatorId(), applicationId), notifyUserCache);
    }

    public static <T extends PlatformData> void updateCacheForNotify(T onboardingData, Long timeRemindInMillis, ApplicationListWrapper applicationListWrapper, RedisCacheStorage cacheStorage) {
        if (Utils.isEmpty(applicationListWrapper) ||
                Utils.isEmpty(applicationListWrapper.getApplicationForms()) ||
                Utils.isEmpty(applicationListWrapper.getApplicationForms().getFirst().getApplicationData())) {
            Log.MAIN.info("Data is empty - skip cache for notify");
            return;
        }
        String applicationId = applicationListWrapper.getApplicationForms().getFirst().getApplicationData().getApplicationId();
        boolean success = cacheStorage.update(ApplicationListWrapper.createKeyNotify(onboardingData.getServiceId(), onboardingData.getInitiatorId(), applicationId), applicationListWrapper, timeRemindInMillis);
        if (!success) {
            addCacheForNotify(onboardingData, timeRemindInMillis, applicationListWrapper, cacheStorage);
        }
    }

    public static <T extends PlatformData> void updateCacheForNotify(T onboardingData, ServiceObInfo serviceObInfo, ApplicationForm applicationForm, RedisCacheStorage cacheStorage) {
        ApplicationListWrapper applicationNotifyWrapper = new ApplicationListWrapper();
        applicationNotifyWrapper.setApplicationForms(List.of(applicationForm));
        updateCacheForNotify(onboardingData, serviceObInfo.getTimeRemindUser().longValue(), applicationNotifyWrapper, cacheStorage);
    }

    private static <T extends PlatformData> CacheData getCacheData(T onboardingData, Long timeRemindInMillis, ApplicationListWrapper applicationListWrapper) {
        CacheData notifyUserCache = new CacheData();
        CacheData.Expiration expiration = new CacheData.Expiration();
        expiration.setInitiator(onboardingData.getInitiator());
        expiration.setInitiatorId(onboardingData.getInitiatorId());
        expiration.setExpirationProcessName("noti-remind");
        expiration.setPartnerId(onboardingData.getPartnerId());
        expiration.setServiceId(onboardingData.getServiceId());
        notifyUserCache.setExpiredTime(timeRemindInMillis);
        notifyUserCache.setExpiration(expiration);
        notifyUserCache.setCacheObject(applicationListWrapper);
        notifyUserCache.setTraceId(onboardingData.getTraceId());
        return notifyUserCache;
    }

    public static void addDataToPrimaryButton(RenderData renderData, String key, String newKey, Object newValue) {
        CtaFeature.FeatureCodeData featureCodeData = (CtaFeature.FeatureCodeData) renderData.getInformationPage().getButtons().getPrimary().getData();
        Map<String, Object> currentObject = JsonUtil.toMap(featureCodeData.getParams().get(key));
        currentObject.put(newKey, newValue);
        featureCodeData.getParams().put(key, currentObject);
    }

    public static boolean isFaceDataSuccess(FaceData faceData) {
        return Utils.isNotEmpty(faceData)
                && CommonErrorCode.SUCCESS.getCode().equals(faceData.getResultCode())
                && FaceDataResult.SUCCESS.name().equals(faceData.getStatus());
    }

    public static long calculatePendingFormExpireTime(long expiredTimeInMillis, ServiceObInfo serviceObInfo) {
        if (expiredTimeInMillis == 0) {
            Log.MAIN.info("ApplicationForm dose not have expired time {}, createdTime {}", expiredTimeInMillis);
            return serviceObInfo.getTimeRemindUser();
        }
        long currentTimeStamp = System.currentTimeMillis();
        long expiredTimeSaveCache = expiredTimeInMillis - currentTimeStamp;
        Log.MAIN.info("expiredTimeSaveCache: expiredTimeInMillis {} - currentTimeStamp {} = {}", expiredTimeInMillis, currentTimeStamp, expiredTimeSaveCache);
        return expiredTimeSaveCache;
    }

    public static void savePendingForm(RedisCacheStorage cacheStorage, String key, CacheData pendingFormCache, long expiredTimeSaveCache) throws Exception {
        if (expiredTimeSaveCache > 0) {
            Log.MAIN.info("Save cache with expiredTimeCache {}", expiredTimeSaveCache);
//            Log.MAIN.info("Cache {}", Json.encode(cacheStorage));
            pendingFormCache.setExpiredTime(expiredTimeSaveCache);
            cacheStorage.put(key, pendingFormCache);
        } else {
            Log.MAIN.info("Delete cache with expiredTimeCache {}", expiredTimeSaveCache);
            cacheStorage.remove(key);
        }
    }

    public static boolean isStoreApplicationData(Integer resultCode, ApplicationStatus applicationStatus) {
        return CommonErrorCode.SUCCESS.getCode().equals(resultCode) ||
                OnboardingErrorCode.LOAN_DECIDER_REJECT.getCode().equals(resultCode) ||
                Constant.STORE_REJECTED_STATUS.contains(applicationStatus);
    }

    public static AiLoanActionConfig mappingLoanDeciderAction(ServiceObInfo serviceObInfo, List<LoanActionType> requiredActionLoanDeciderList, UserProfileInfo userProfileInfo) {

        List<String> aiActionNames = new ArrayList<>();

        for (LoanActionType actionType : requiredActionLoanDeciderList) {
            aiActionNames.add(actionType.name());
        }

        List<String> userProfileRule = new ArrayList<>();
        AIActionMappingConfig currentAIActionMapping = new AIActionMappingConfig();
        String resultMatch = userProfileInfo.getFaceMatching() == FaceMatching.MATCHED ? UserProfileConfigValue.IS_FACE_MATCH.name() : UserProfileConfigValue.FACE_NOT_MATCH.name();
        userProfileRule.add(resultMatch);

        currentAIActionMapping.setUserProfileInfos(userProfileRule);
        currentAIActionMapping.setAiLoanActionNames(aiActionNames);

        Log.MAIN.info("User AI action mapping {}", Json.encode(currentAIActionMapping));
        for (AiLoanActionConfig aiLoanActionConfig : serviceObInfo.getAiLoanActionConfigs()) {
            if (aiLoanActionConfig.isMapConfig(userProfileRule, aiActionNames)) {
                Log.MAIN.info("AI loan action map success {} - {}", Json.encode(currentAIActionMapping), Json.encode(aiLoanActionConfig));
                return aiLoanActionConfig;
            }
        }

        return null;
    }

    public static void putDateToTemplateMap(Map<String, Object> templateMap, long unlockOtpTimeInMillis) {
        Log.MAIN.info("Put date to template map");
        templateMap.put(DATE_UNLOCK_OTP, new UnLockOtpDate(unlockOtpTimeInMillis));
    }

    public static String convertInterestToString(double interest) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat("#.#######", symbols);
        return df.format(interest * 100) + "%";
    }

    public static String convertLongToDateFormat(long epochMilli) {
        Date date = new Date(epochMilli);
        SimpleDateFormat formatter = new SimpleDateFormat(Constant.ONBOARDING_DATE_FORMAT);
        return formatter.format(date);
    }

    public static void mappingApplicationData(ApplicationData obj1, ApplicationData obj2) throws Exception {
        Map<String, Field> mapFields = Generics.getMapAllFields(obj1.getClass());
        Generics.scanFields(ApplicationData.class, field -> {
                    Field infoField = mapFields.get(field.getName());
                    if (infoField != null) {
                        field.setAccessible(true);
                        infoField.setAccessible(true);
                        Object valueInfoField = infoField.get(obj1);
                        if (Utils.isNotEmpty(valueInfoField)) {
                            if (Map.class.isAssignableFrom(infoField.getType())) {
                                updateApplicationAdditionalData(obj1.getApplicationAdditionalData(), obj2, field);
                            } else {
                                field.set(obj2, valueInfoField);
                            }
                        }
                    }
                }
        );
    }

    public static ErrorCode getErrorCode(int resultCode) {
        return new ErrorCode() {

            @Override
            public Integer getCode() {
                return resultCode;
            }

            @Override
            public BackendStatus status() {
                if (CommonErrorCode.SUCCESS.getCode().intValue() == resultCode) {
                    return BackendStatus.SUCCESS;
                } else {
                    return BackendStatus.FAIL;
                }
            }

            @Override
            public BackendFailureReason failureReason() {
                return BackendFailureReason.EMPTY;
            }

            @Override
            public String getMessage() {
                return CommonConstant.STRING_EMPTY;
            }
        };
    }

    public static boolean isZeroInterestPackage(PackageInfo packageInfo) {
        return packageInfo.getInterest() == 0;
    }

    //Create lenderId for service not have chosen package like CLO
    public static void createDataForEmptyPackage(ApplicationForm applicationForm, PackageCache packageCache) {
        ApplicationData applicationData = applicationForm.getApplicationData();
        PackageInfo chosenPackage = applicationData.getChosenPackage();

        if (Utils.isNotEmpty(chosenPackage)) {
            Log.MAIN.info("Have chosen package => not create data chosenPackage");
            return;
        }

        if (Utils.isEmpty(packageCache) || Utils.isEmpty(packageCache.getGetPackageResponse()) || Utils.isEmpty(packageCache.getGetPackageResponse().getLenderId())) {
            Log.MAIN.info("packageCache {} , PackageResponse {}, lenderId {} => return", packageCache, packageCache.getGetPackageResponse(), packageCache.getGetPackageResponse().getLenderId());
            return;
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setLenderId(packageCache.getGetPackageResponse().getLenderId());
        Log.MAIN.info("Set chosenPackage {} to applicationData", chosenPackage);
        applicationData.setChosenPackage(packageInfo);
    }

    public static void setImageApplicationFromUserProfile(ApplicationData applicationData, UserProfileInfo userProfileInfo) {
        if (Utils.isEmpty(userProfileInfo) || Utils.isEmpty(applicationData)) {
            return;
        }
        Image frontPersonalImage = new Image();
        frontPersonalImage.setPath(userProfileInfo.getPathFrontImage());
        frontPersonalImage.setUrl(userProfileInfo.getIdFrontImageKyc());
        Image backPersonalImage = new Image();
        backPersonalImage.setPath(userProfileInfo.getPathBackImage());
        backPersonalImage.setUrl(userProfileInfo.getIdBackImageKyc());
        Image factMatchingImage = new Image();
        factMatchingImage.setPath(userProfileInfo.getPathMainFaceImage());
        factMatchingImage.setUrl(userProfileInfo.getImageFaceMatching());

        applicationData.setFrontPersonalIdImage(frontPersonalImage);
        applicationData.setBackPersonalIdImage(backPersonalImage);
        applicationData.setFaceMatchingImage(factMatchingImage);
    }

    public static UserActionEvent buildUserActionEvent(ScamAlertResult scamAlertResult) {
        long now = System.currentTimeMillis();
        ActionType actionType = ActionType.findByType(scamAlertResult.getActionType());
        UserActionEvent event = new UserActionEvent();
        event.setActionType(actionType.getType());
        event.setActionId(actionType.getId());
        event.setEndTimestamp(now);
        event.setStartTimestamp(now - scamAlertResult.getDurationInMillis());
        event.setActionStatus(ActionStatus.COMPLETED.name());
        return event;
    }

    public static void setInterestRate(List<String> whiteListPackageInterestRate, PackageInfo packageInfo) {
        if (whiteListPackageInterestRate.contains(packageInfo.getPackageCode())) {
            packageInfo.setInterestRate(convertInterestToInterestRate(packageInfo.getInterest()));
        }
    }

    public static String convertInterestToInterestRate(double interest) {
        String formattedInterest = String.format("%.1f", interest * 100);
        return formattedInterest.replace('.', ',') + "%/năm";
    }


    public static String getDueDate(ServiceObInfo serviceObInfo, String lenderId) throws ScriptException {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();

        List<FormulaDueDateConfig> dueDateConfig = serviceObInfo.getFormulaDueDateConfig();
        for (FormulaDueDateConfig config : dueDateConfig) {
            if (config.getLenderId().equals(lenderId) && day >= config.getStartDay() && day <= config.getEndDay()) {

                return calculatePaymentDate(today, config);
            }
        }

        return null;
    }

    private static String calculatePaymentDate(LocalDate loanDate, FormulaDueDateConfig config) {
        if (config.getWithDay() != 0) {
            loanDate = loanDate.withDayOfMonth(config.getWithDay());
        }
        if (config.getPlusDay() != 0) {
            loanDate = loanDate.plusDays(config.getPlusDay());
        }
        if (config.getPlusMonth() != 0) {
            loanDate = loanDate.plusMonths(config.getPlusMonth());
        }

        return loanDate.format(DateTimeFormatter.ofPattern(DateUtil.MOMO_DATE_FORMAT));
    }

    public static boolean isPackageEmpty(Map<String, PackageInfo> packageInfoMap) {
        if (Utils.isEmpty(packageInfoMap)) {
            return true;
        }
        for (Map.Entry<String, PackageInfo> entry : packageInfoMap.entrySet()) {
            PackageInfo packageInfo = entry.getValue();
            if (PackageStatus.AVAILABLE.name().equals(packageInfo.getPackageStatus())) {
                return false;
            }
        }
        return true;
    }

    public static long computeDailyEquivalentAmount(long emi) {
        YearMonth currentYearMonth = YearMonth.now();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        double raw = (double) emi / daysInMonth;
        return Math.round(raw);
    }

    public static <T> T copy(T entity) throws Exception {
        if (entity == null) {
            return null;
        }
        Class<?> clazz = entity.getClass();
        T newEntity = (T) entity.getClass().getDeclaredConstructor().newInstance();

        for (Field field : Generics.getAllFields(clazz)) {
            field.setAccessible(true);
            field.set(newEntity, field.get(entity));
        }

        return newEntity;
    }

    public static <T> T deepCopy(T entity) throws Exception {
        try {
            if (entity == null) {
                return null;
            }
            Class<?> clazz = entity.getClass();
            Constructor<T> ctor = (Constructor<T>) clazz.getDeclaredConstructor(); // ()
            if (!ctor.canAccess(null)) {
                ctor.setAccessible(true);
            }
            T newEntity = (T) ctor.newInstance();

            for (Field field : Generics.getAllFields(clazz)) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                    continue;
                }

                if (!field.canAccess(entity)) {
                    field.setAccessible(true);
                }

                Object value = field.get(entity);
                if (value == null) {
                    field.set(newEntity, null);
                } else if (isPrimitiveOrWrapper(value.getClass())) {
                    // immutable: copy trực tiếp
                    field.set(newEntity, value);
                } else if (value instanceof Collection) {
                    // deep copy collection
                    Collection<?> srcCol = (Collection<?>) value;
                    Collection<Object> newCol = srcCol.getClass().getDeclaredConstructor().newInstance();
                    for (Object item : srcCol) {
                        newCol.add(deepCopy(item));
                    }
                    field.set(newEntity, newCol);
                } else if (value instanceof Map) {
                    // deep copy map
                    Map<?, ?> srcMap = (Map<?, ?>) value;
                    Map<Object, Object> newMap = srcMap.getClass().getDeclaredConstructor().newInstance();
                    for (Map.Entry<?, ?> entry : srcMap.entrySet()) {
                        Object k = entry.getKey();
                        Object v = entry.getValue();
                        newMap.put(k, v);
                    }
                    field.set(newEntity, newMap);
                } else {
                    // object khác: đệ quy
                    field.set(newEntity, deepCopy(value));
                }
            }

            return newEntity;
        } catch (Exception e) {
            Log.MAIN.error("Error copy: ", e);
            return null;
        }

    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || type.equals(String.class)
                || type.equals(Boolean.class)
                || type.equals(Byte.class)
                || type.equals(Character.class)
                || type.equals(Short.class)
                || type.equals(Integer.class)
                || type.equals(Long.class)
                || type.equals(Float.class)
                || type.equals(Double.class);
    }
}
