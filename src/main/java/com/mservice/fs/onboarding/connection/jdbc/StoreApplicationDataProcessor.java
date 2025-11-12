package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.MappingRegistry;
import com.mservice.fs.jdbc.mapping.UnsupportedDataTypeException;
import com.mservice.fs.jdbc.processor.InputParamWrapper;
import com.mservice.fs.jdbc.processor.MultiStatementProcessor;
import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.config.MultiStoreConfig;
import com.mservice.fs.onboarding.enums.AddressType;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.Address;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.LoanDeciderData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ReferencePeople;
import com.mservice.fs.onboarding.model.application.init.AdditionalDataDB;
import com.mservice.fs.onboarding.model.application.init.AddressDB;
import com.mservice.fs.onboarding.model.application.init.ApplicationDB;
import com.mservice.fs.onboarding.model.application.init.PackageInfoDB;
import com.mservice.fs.onboarding.model.application.init.RelativeDb;
import com.mservice.fs.onboarding.model.application.init.UserInfoDB;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author hoang.thai
 * on 11/8/2023
 */
public class StoreApplicationDataProcessor extends MultiStatementProcessor<MultiStoreConfig> {


    public void store(ApplicationForm applicationForm, OnboardingData onboardingData) throws UnsupportedDataTypeException, Exception {
        InputParamWrapper applicationParamWrapper = new InputParamWrapper(getConfig().getStoreApplicationInfo());
        ApplicationData applicationData = applicationForm.getApplicationData();
        Double momoCreditScore = applicationForm.getApplicationData().getCurrentCreditScore();
        ApplicationDB applicationDB = getApplicationDB(applicationData, onboardingData, momoCreditScore);
        applicationParamWrapper.addBatch(MappingRegistry.convertToParams(applicationDB));

        InputParamWrapper addressParamWrapper = new InputParamWrapper(getConfig().getStoreAddressInfo());
        AddressDB companyAddressDB = createAddressDB(applicationData, applicationData.getCompanyAddress(), AddressType.COMPANY);
        AddressDB shippingAddressDB = createAddressDB(applicationData, applicationData.getShippingAddress(), AddressType.SHIPPING);
        AddressDB currentAddressDB = createAddressDB(applicationData, applicationData.getCurrentAddress(), AddressType.CURRENT);
        AddressDB permanentAddressDB = createAddressDB(applicationData, applicationData.getPermanentAddress(), AddressType.PERMANENT);
        AddressDB placeOfBirthAddressDB = createAddressDB(applicationData, applicationData.getPlaceOfBirth(), AddressType.PLACE_OF_BIRTH);
        AddressDB modifiedPermanentAddress = createAddressDB(applicationData, applicationData.getCurrentAddress(), AddressType.USER);
        addBatchAddressParamWrapper(addressParamWrapper, companyAddressDB, shippingAddressDB, currentAddressDB, permanentAddressDB, placeOfBirthAddressDB, modifiedPermanentAddress);

        InputParamWrapper userInfoParamWrapper = new InputParamWrapper(getConfig().getStoreUser());
        // TODO: 11/9/2023 get second param to User profile
        UserInfoDB userInfoDB = createUserInfoDB(applicationData);
        userInfoParamWrapper.addBatch(MappingRegistry.convertToParams(userInfoDB));

        InputParamWrapper referencePeopleParamWrapper = new InputParamWrapper(getConfig().getStoreRelativeInfo());
        addBatchReferencePeopleParamWrapper(applicationData, referencePeopleParamWrapper);

        InputParamWrapper packageInfoParamWrapper = new InputParamWrapper(getConfig().getStorePackageInfo());
        addBatchPackageInfoWrapper(applicationData, packageInfoParamWrapper);

        InputParamWrapper additionalDataParamWrapper = new InputParamWrapper(getConfig().getStoreAdditionalData());
        addBatchAdditionalDataParamWrapper(applicationData, additionalDataParamWrapper);

        execute(applicationParamWrapper, addressParamWrapper, userInfoParamWrapper, referencePeopleParamWrapper, packageInfoParamWrapper, additionalDataParamWrapper);
    }

    private void addBatchAdditionalDataParamWrapper(ApplicationData applicationData, InputParamWrapper additionalDataParamWrapper) throws UnsupportedDataTypeException, Exception {
        Map<String, Object> additionalData = applicationData.getApplicationAdditionalData();
        if (Utils.isEmpty(additionalData)) {
            return;
        }
        for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            AdditionalDataDB additionalDataDB = AdditionalDataDB.builder()
                    .contractId(applicationData.getApplicationId())
                    .key(entry.getKey())
                    .value(String.valueOf(entry.getValue()))
                    .build();
            additionalDataParamWrapper.addBatch(MappingRegistry.convertToParams(additionalDataDB));
        }
    }

    private void addBatchPackageInfoWrapper(ApplicationData applicationData, InputParamWrapper packageInfoParamWrapper) throws UnsupportedDataTypeException, Exception {
        PackageInfo packageInfo = applicationData.getChosenPackage();
        if (packageInfo == null) {
            return;
        }
        packageInfoParamWrapper.addBatch(MappingRegistry.convertToParams(createPackageInfoDB(packageInfo, applicationData.getApplicationId())));
    }

    private PackageInfoDB createPackageInfoDB(PackageInfo packageInfo, String applicationId) {
        return PackageInfoDB.builder()
                .contractId(applicationId)
                .packageGroup(packageInfo.getPackageGroup())
                .packageName(packageInfo.getPackageName())
                .packageCode(packageInfo.getPackageCode())
                .lenderId(packageInfo.getLenderId())
                .rank(packageInfo.getRank())
                .tenor(packageInfo.getTenor())
                .loanAmount(packageInfo.getLoanAmount())
                .disbursedAmount(packageInfo.getDisbursedAmount())
                .interestAmount(packageInfo.getInterestAmount())
                .interestUnit(Utils.isEmpty(packageInfo.getInterestUnit()) ? null : packageInfo.getInterestUnit().name())
                .serviceFee(packageInfo.getServiceFee())
                .collectionFee(packageInfo.getCollectionFee())
                .disbursedFee(packageInfo.getDisbursedFee())
                .lateInterest(packageInfo.getLateInterest())
                .lateFee(packageInfo.getLateFee())
                .interest(packageInfo.getInterest())
                .paymentAmount(packageInfo.getPaymentAmount())
                .emi(packageInfo.getEmi())
                .tenorUnit(Utils.isEmpty(packageInfo.getTenorUnit()) ? CommonConstant.STRING_EMPTY : packageInfo.getTenorUnit().name())
                .segmentUser(packageInfo.getSegmentUser())
                .lenderLogic(packageInfo.getLenderLogic())
                .dueDay(packageInfo.getDueDay())
                .partnerId(packageInfo.getPartnerId())
                .packageMapName(packageInfo.getPackageMapName())
                .lenderName(packageInfo.getLenderName())
                .productGroup(packageInfo.getProductGroup())
                .build();
    }

    private void addBatchReferencePeopleParamWrapper(ApplicationData applicationData, InputParamWrapper referencePeopleParamWrapper) throws UnsupportedDataTypeException, Exception {
        List<ReferencePeople> referencePeople = applicationData.getReferencePeople();
        if (Utils.isEmpty(referencePeople)) {
            return;
        }
        for (ReferencePeople people : referencePeople) {
            if (Utils.isEmpty(people)) {
                continue;
            }
            referencePeopleParamWrapper.addBatch(
                    MappingRegistry.convertToParams(
                            RelativeDb.builder()
                                    .referenceId(applicationData.getApplicationId())
                                    .fullName(people.getFullName())
                                    .phoneNumber(people.getPhoneNumber())
                                    .code(people.getRelationship().getId())
                                    .relative(people.getRelationship().getName())
                                    .partnerCode(applicationData.getPartnerId())
                                    .contractId(applicationData.getApplicationId())
                                    .build()));
        }
    }

    private UserInfoDB createUserInfoDB(ApplicationData applicationData) {
        return UserInfoDB.builder()
                .referenceId(applicationData.getApplicationId())
                .contractId(applicationData.getApplicationId())
                .fullName(applicationData.getFullName())
                .gender(Utils.isEmpty(applicationData.getGender()) ? null : applicationData.getGender().name())
                .dob(applicationData.getDob())
                .email(applicationData.getEmail())
                .nationality(applicationData.getNationality())
                .taxCode(applicationData.getTaxId())
                .personalId(applicationData.getIdNumber())
                .income(applicationData.getIncome())
                .issueDate(applicationData.getIssueDate())
                .issuePlace(applicationData.getIssuePlace())
                .idType(Utils.isEmpty(applicationData.getIdType()) ? null : applicationData.getIdType().name())
                .expiryDate(applicationData.getExpiryDate())
                .partnerId(applicationData.getPartnerId())
                .modifiedName(applicationData.getModifiedName())
                .frontImagePath(Utils.isEmpty(applicationData.getFrontPersonalIdImage()) ? null : applicationData.getFrontPersonalIdImage().getPath())
                .backImagePath(Utils.isEmpty(applicationData.getBackPersonalIdImage()) ? null : applicationData.getBackPersonalIdImage().getPath())
                .faceMatchingImagePath(Utils.isEmpty(applicationData.getFaceMatchingImage()) ? null : applicationData.getFaceMatchingImage().getPath())
                .frontImageUrl(Utils.isEmpty(applicationData.getFrontPersonalIdImage()) ? null : applicationData.getFrontPersonalIdImage().getUrl())
                .backImageUrl(Utils.isEmpty(applicationData.getBackPersonalIdImage()) ? null : applicationData.getBackPersonalIdImage().getUrl())
                .faceMatchingImageUrl(Utils.isEmpty(applicationData.getFaceMatchingImage()) ? null : applicationData.getFaceMatchingImage().getUrl())
                .build();
    }

    private void addBatchAddressParamWrapper(InputParamWrapper addressParamWrapper, AddressDB... addressDBs) throws UnsupportedDataTypeException, Exception {
        for (AddressDB addressDB : addressDBs) {
            if (Utils.isEmpty(addressDB)) {
                continue;
            }
            addressParamWrapper.addBatch(MappingRegistry.convertToParams(addressDB));
        }
    }

    private AddressDB createAddressDB(ApplicationData applicationData, Address address, AddressType addressType) {
        if (Utils.isEmpty(address)) {
            return null;
        }
        AddressDB addressDB = new AddressDB();
        addressDB.setReferenceId(applicationData.getApplicationId());
        addressDB.setFullAddress(address.getFullAddress());
        addressDB.setStreet(address.getStreet());
        if (address.getWard() != null) {
            addressDB.setWardCode(address.getWard().getCode());
            addressDB.setWardName(address.getWard().getName());
        }
        if (address.getDistrict() != null) {
            addressDB.setDistrictCode(address.getDistrict().getCode());
            addressDB.setDistrictName(address.getDistrict().getName());
        }
        if (address.getProvince() != null) {
            addressDB.setProvinceCode(address.getProvince().getCode());
            addressDB.setProvinceName(address.getProvince().getName());
        }
        addressDB.setAddressType(addressType.name());
        addressDB.setPartnerCode(applicationData.getPartnerId());
        addressDB.setContractId(applicationData.getApplicationId());
        return addressDB;
    }

    private ApplicationDB getApplicationDB(ApplicationData applicationData, OnboardingData onboardingData, Double momoCreditScore) throws Exception {
        return ApplicationDB.builder()
                .contractId(applicationData.getApplicationId())
                .referenceId(applicationData.getApplicationId())
                .agentId(applicationData.getAgentId())
                .phoneNumber(applicationData.getPhoneNumber())
                .partnerId(applicationData.getPartnerId())
                .serviceId(applicationData.getServiceId())
                .status(applicationData.getStatus().name())
                .state(applicationData.getStatus().getState().name())
                .momoCreditScore(momoCreditScore)
                .taxCode(applicationData.getTaxId())
                .initiator(onboardingData.getInitiator())
                .partnerApplicationId(applicationData.getPartnerApplicationId())
                .reasonId(applicationData.getReasonId())
                .reasonMessage(applicationData.getReasonMessage())
                .paymentMerchantInfo(getTotalPaymentInfoRecord(applicationData.getLoanDeciderData()))
                .socialSellerProfile(getSocialSellerProfileRecord(applicationData.getLoanDeciderData()))
                .telco(applicationData.getTelco())
                .build();
    }

    private String getTotalPaymentInfoRecord(LoanDeciderData loanDeciderData) {
        if (Utils.isEmpty(loanDeciderData)) {
            return null;
        }
        if (Utils.isEmpty(loanDeciderData.getMerchantProfile())) {
            return null;
        }
        return Json.encode(loanDeciderData.getMerchantProfile().getTotalPaymentInfoRecord());
    }

    private String getSocialSellerProfileRecord(LoanDeciderData loanDeciderData) {
        if (Utils.isEmpty(loanDeciderData)) {
            return null;
        }
        if (Utils.isEmpty(loanDeciderData.getMerchantProfile())) {
            return null;
        }
        return Json.encode(loanDeciderData.getMerchantProfile().getSocialSellerProfileRecord());
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }
}
