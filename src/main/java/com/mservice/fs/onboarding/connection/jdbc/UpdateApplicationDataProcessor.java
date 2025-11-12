package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.MappingRegistry;
import com.mservice.fs.jdbc.mapping.UnsupportedDataTypeException;
import com.mservice.fs.jdbc.processor.InputParamWrapper;
import com.mservice.fs.jdbc.processor.MultiStatementProcessor;
import com.mservice.fs.onboarding.config.MultiUpdateConfig;
import com.mservice.fs.onboarding.enums.AddressType;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.Address;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ReferencePeople;
import com.mservice.fs.onboarding.model.api.update.UpdateApplicationRequest;
import com.mservice.fs.onboarding.model.application.init.AdditionalDataDB;
import com.mservice.fs.onboarding.model.application.init.AddressDB;
import com.mservice.fs.onboarding.model.application.init.RelativeDb;
import com.mservice.fs.onboarding.model.application.update.UpdatePackageDB;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author muoi.nong
 */
public class UpdateApplicationDataProcessor extends MultiStatementProcessor<MultiUpdateConfig> {

    public void store(UpdateApplicationRequest request, OnboardingData onboardingData) throws UnsupportedDataTypeException, Exception {

        InputParamWrapper addressParamWrapper = new InputParamWrapper(getConfig().getUpdateAddressInfo());
        if (Utils.isNotEmpty(request.getAddressInfo())) {

            AddressDB shippingAddressDB = createAddressDB(request, onboardingData, request.getAddressInfo().getContactAddress(), AddressType.SHIPPING);
            AddressDB currentAddressDB = createAddressDB(request, onboardingData, request.getAddressInfo().getCurrentAddress(), AddressType.CURRENT);
            AddressDB modifiedPermanentAddress = createAddressDB(request, onboardingData, request.getAddressInfo().getModifiedPermanentAddress(), AddressType.USER);
            AddressDB companyAddress = createAddressDB(request, onboardingData, request.getAddressInfo().getCompanyAddress(), AddressType.COMPANY);
            addBatchAddressParamWrapper(addressParamWrapper, shippingAddressDB, currentAddressDB, modifiedPermanentAddress, companyAddress);
        }

        InputParamWrapper referencePeopleParamWrapper = new InputParamWrapper(getConfig().getUpdateRelativeInfo());
        addBatchReferencePeopleParamWrapper(request, onboardingData, referencePeopleParamWrapper);

        InputParamWrapper additionalDataParamWrapper = new InputParamWrapper(getConfig().getUpdateAdditionalData());
        addBatchAdditionalDataParamWrapper(request, additionalDataParamWrapper);


        InputParamWrapper packageParamWrapper = new InputParamWrapper(getConfig().getUpdatePackage());
        addBatchPackageParamWrapper(request, packageParamWrapper);

        execute(addressParamWrapper, referencePeopleParamWrapper, additionalDataParamWrapper, packageParamWrapper);
    }

    private void addBatchPackageParamWrapper(UpdateApplicationRequest request, InputParamWrapper packageParamWrapper) throws UnsupportedDataTypeException, Exception {
        PackageInfo packageInfo = Utils.isNotEmpty(request.getPackageInfo()) ? request.getPackageInfo() : new PackageInfo();
        UpdatePackageDB updatePackageDB = UpdatePackageDB.builder()
                .applicationId(request.getApplicationId())
                .tenor(packageInfo.getTenor())
                .emi(packageInfo.getEmi())
                .loanAmount(packageInfo.getLoanAmount())
                .income(request.getIncome())
                .email(request.getEmail())
                .monthlyInterestRate(packageInfo.getMonthlyInterestRate())
                .reasonMessage(request.getReasonMessage())
                .partnerApplicationId(request.getPartnerApplicationId())
                .build();
        packageParamWrapper.addBatch(MappingRegistry.convertToParams(updatePackageDB));
    }

    private void addBatchAdditionalDataParamWrapper(UpdateApplicationRequest request, InputParamWrapper additionalDataParamWrapper) throws UnsupportedDataTypeException, Exception {
        Map<String, Object> additionalData = request.getApplicationAdditionalData();
        if (Utils.isEmpty(additionalData)) {
            return;
        }
        for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            AdditionalDataDB additionalDataDB = AdditionalDataDB.builder()
                    .contractId(request.getApplicationId())
                    .key(entry.getKey())
                    .value(String.valueOf(entry.getValue()))
                    .build();
            additionalDataParamWrapper.addBatch(MappingRegistry.convertToParams(additionalDataDB));
        }
    }

    private void addBatchReferencePeopleParamWrapper(UpdateApplicationRequest request, OnboardingData onboardingData, InputParamWrapper referencePeopleParamWrapper) throws UnsupportedDataTypeException, Exception {
        List<ReferencePeople> referencePeople = request.getReferencePeople();
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
                                    .referenceId(request.getApplicationId())
                                    .fullName(people.getFullName())
                                    .phoneNumber(people.getPhoneNumber())
                                    .code(people.getRelationship().getId())
                                    .relative(people.getRelationship().getName())
                                    .partnerCode(onboardingData.getPartnerId())
                                    .contractId(request.getApplicationId())
                                    .nationalId(people.getNationalId())
                                    .build()));
        }
    }

    private void addBatchAddressParamWrapper(InputParamWrapper addressParamWrapper, AddressDB... addressDBs) throws UnsupportedDataTypeException, Exception {
        for (AddressDB addressDB : addressDBs) {
            if (Utils.isEmpty(addressDB)) {
                continue;
            }
            addressParamWrapper.addBatch(MappingRegistry.convertToParams(addressDB));
        }
    }

    private AddressDB createAddressDB(UpdateApplicationRequest request, OnboardingData onboardingData, Address address, AddressType addressType) {
        if (Utils.isEmpty(address)) {
            return null;
        }

        AddressDB addressDB = new AddressDB();
        addressDB.setReferenceId(request.getApplicationId());
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
        addressDB.setPartnerCode(onboardingData.getPartnerId());
        addressDB.setContractId(request.getApplicationId());
        return addressDB;
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }
}
