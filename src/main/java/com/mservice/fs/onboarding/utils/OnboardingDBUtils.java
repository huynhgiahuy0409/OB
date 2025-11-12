package com.mservice.fs.onboarding.utils;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.AddressType;
import com.mservice.fs.onboarding.model.Address;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileContractLink;
import com.mservice.fs.onboarding.model.Image;
import com.mservice.fs.onboarding.model.KeyValue;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ReferencePeople;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.sof.queue.model.profile.Gender;
import com.mservice.fs.sof.queue.model.profile.IdType;
import com.mservice.fs.utils.Utils;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class OnboardingDBUtils {

    private OnboardingDBUtils() {
    }

    public static List<ApplicationData> loadListSubmitedForm(CallableStatement cs) throws SQLException, BaseException {
        ResultSet rsApplication = (ResultSet) cs.getObject("P_RESULT");
        List<ApplicationData> applicationDatas = new ArrayList<>();
        while (rsApplication.next()) {
            ApplicationData applicationData = new ApplicationData();
            applicationData.setApplicationId(rsApplication.getString("REFERENCE_ID"));
            applicationData.setPartnerId(rsApplication.getString("PACKAGE_CODE"));
            applicationData.setFullName(rsApplication.getString(Constant.FULL_NAME));
            applicationData.setDob(rsApplication.getString("DOB"));
            applicationData.setIdNumber(rsApplication.getString("PERSONAL_ID"));
            applicationData.setEmail(rsApplication.getString("EMAIL"));
            applicationData.setGender(Gender.valueOf(rsApplication.getString("NEW_GENDER")));
            applicationData.setIncome(rsApplication.getLong("MONTHLY_INCOME"));
            applicationData.setAgentId(rsApplication.getString("AGENT_ID"));
            applicationData.setTaxId(rsApplication.getString(Constant.TAX_CODE));
            applicationDatas.add(applicationData);
        }
        return applicationDatas;
    }

    public static ApplicationData   loadApplicationData(CallableStatement cs) throws Exception {
        ResultSet rsAppInfo = (ResultSet) cs.getObject("P_APPLICATION_INFO");

        ApplicationData application = null;
        if (rsAppInfo.next()) {
            application = new ApplicationData();
            application.setEmail(rsAppInfo.getString("EMAIL"));
            application.setFullName(rsAppInfo.getString(Constant.FULL_NAME));
            application.setFrontPersonalIdImage(getImage(rsAppInfo.getString("FRONT_IMAGE_PATH"), rsAppInfo.getString("FRONT_IMAGE_URL")));
            application.setBackPersonalIdImage(getImage(rsAppInfo.getString("BACK_IMAGE_PATH"), rsAppInfo.getString("BACK_IMAGE_URL")));
            application.setFaceMatchingImage(getImage(rsAppInfo.getString("FACE_MATCHING_IMAGE_PATH"), rsAppInfo.getString("FACE_MATCHING_IMAGE_URL")));
            List<ReferencePeople> referencePeople = getReferencePeople(cs);
            loadAddressInfo(application, cs);
            application.setReferencePeople(referencePeople);
            application.setTaxId(rsAppInfo.getString(Constant.TAX_CODE));
            PackageInfo packageInfo = getPackageInfo(cs, rsAppInfo);
            application.setChosenPackage(packageInfo);
            application.setAgentId(rsAppInfo.getString("AGENT_ID"));
            application.setDob(rsAppInfo.getString("DOB"));
            String idType = rsAppInfo.getString("ID_TYPE");
            if (Utils.isNotEmpty(idType)) {
                application.setIdType(IdType.valueOf(idType));
            }
            application.setTaxId(rsAppInfo.getString(Constant.TAX_CODE));
            String gender = rsAppInfo.getString("NEW_GENDER");
            if (Utils.isNotEmpty(gender)) {
                application.setGender(Gender.valueOf(gender));
            }
            application.setIdNumber(rsAppInfo.getString("PERSONAL_ID"));
            application.setPhoneNumber(rsAppInfo.getString("PHONE_NUMBER"));
            application.setInitiator(rsAppInfo.getString("INITIATOR"));
            application.setPartnerId(rsAppInfo.getString("PARTNER_CODE"));
            application.setStatus(ApplicationStatus.valueOf(rsAppInfo.getString("NEW_STATUS")));
            application.setApplicationId(rsAppInfo.getString("CONTRACT_ID"));
            application.setActive(rsAppInfo.getInt("ACTIVE") == 1);
            Timestamp createdTime = rsAppInfo.getTimestamp("CREATE_TIME");
            if (createdTime != null) {
                application.setCreatedDate(createdTime.getTime());
            }
            Timestamp lastModifiedTime = rsAppInfo.getTimestamp("LAST_MODIFIED");
            if (lastModifiedTime != null) {
                application.setModifiedDateInMillis(lastModifiedTime.getTime());
            }
            application.setReasonId(rsAppInfo.getInt("REASON_ID"));
            application.setReasonMessage(rsAppInfo.getString("REASON_MESSAGE"));
            Map<ContractType, FileContractLink> fileContractLinkMap = getFileContractLinkData(cs);
            application.setFileContractData(fileContractLinkMap);

            Double momoCreditScore = rsAppInfo.getDouble("MOMO_CREDIT_SCORE");
            if (Utils.isNotEmpty(rsAppInfo.getDouble("MOMO_CREDIT_SCORE"))) {
                application.setCurrentCreditScore(momoCreditScore);
            }

            application.setPartnerApplicationId(rsAppInfo.getString("PARTNER_REFERENCE_ID"));
            application.setIncome(rsAppInfo.getLong("MONTHLY_INCOME"));
            application.setIssueDate(Utils.coalesce(rsAppInfo.getString("NEW_ISSUE_DATE"), rsAppInfo.getString("ID_ISSUE_DATE")));
            application.setIssuePlace(rsAppInfo.getString("ID_ISSUE_PLACE"));
            application.setExpiryDate(Utils.coalesce(rsAppInfo.getString("NEW_EXPIRY_DATE"), rsAppInfo.getString("EXPIRY_DATE")));
        }
        return application;
    }

    public static void loadAdditionalData(ApplicationData application, CallableStatement cs) throws SQLException {
        if (Utils.isEmpty(application)) {
            Log.MAIN.info("Application is null - skip loadAdditionalData");
            return;
        }
        ResultSet rsAdditionalData = (ResultSet) cs.getObject("P_ADDITIONAL_DATA");
        if (Utils.isEmpty(rsAdditionalData)) {
            Log.MAIN.info("Additional data is null return..");
        }
        Map<String, Object> additionalData = new HashMap<>();

        while (rsAdditionalData.next()) {
            String key = rsAdditionalData.getString("KEY");
            String value = rsAdditionalData.getString("VALUE");
            additionalData.put(key, value);
        }

        if (Utils.isNotEmpty(additionalData)) {
            application.setApplicationAdditionalData(additionalData);
        }
    }

    private static Image getImage(String path, String url) {
        if (Utils.isEmpty(path) && Utils.isEmpty(url)) {
            return null;
        }
        Image image = new Image();
        image.setPath(path);
        image.setUrl(url);
        return image;
    }

    private static void loadAddressInfo(ApplicationData application, CallableStatement cs) throws SQLException {
        ResultSet rsAddressInfo = (ResultSet) cs.getObject("P_ADDRESS_INFO");
        if (Utils.isEmpty(rsAddressInfo)) {
            Log.MAIN.info("Address info is null return..");
            return;
        }
        while (rsAddressInfo.next()) {
            String addressTypeString = rsAddressInfo.getString("ADDRESS_TYPE");
            if (Utils.isEmpty(addressTypeString)) {
                Log.MAIN.info("Address type is null.");
                continue;
            }
            AddressType addressType;
            try {
                addressType = AddressType.valueOf(addressTypeString);
            } catch (Exception ex) {
                Log.MAIN.error("Error when cast with addressType {}", addressTypeString);
                continue;
            }

            String fullAddress = rsAddressInfo.getString("FULL_ADDRESS");
            String street = rsAddressInfo.getString("STREET");

            KeyValue wardKeyValue = convertToKeyValueObject(rsAddressInfo.getString("WARD_CODE"), rsAddressInfo.getString("WARD_NAME"));
            KeyValue districtKeyValue = convertToKeyValueObject(rsAddressInfo.getString("DISTRICT_CODE"), rsAddressInfo.getString("DISTRICT_NAME"));
            KeyValue provinceKeyValue = convertToKeyValueObject(rsAddressInfo.getString("PROVINCE_CODE"), rsAddressInfo.getString("PROVINCE_NAME"));

            Address address = new Address();
            address.setFullAddress(fullAddress);
            address.setStreet(street);
            address.setWard(wardKeyValue);
            address.setDistrict(districtKeyValue);
            address.setProvince(provinceKeyValue);
            switch (addressType) {
                case COMPANY -> application.setCompanyAddress(address);
                case CURRENT -> application.setCurrentAddress(address);
                case SHIPPING -> application.setShippingAddress(address);
                case PERMANENT -> application.setPermanentAddress(address);
                case PLACE_OF_BIRTH -> application.setPlaceOfBirth(address);
                case USER -> application.setModifiedPermanentAddress(address);
            }
        }
    }

    private static KeyValue convertToKeyValueObject(String key, String value) {
        if (Utils.isEmpty(key) && Utils.isEmpty(value)) {
            return null;
        }
        KeyValue keyValue = new KeyValue();
        keyValue.setId(key);
        keyValue.setName(value);
        return keyValue;
    }

    private static Integer convertStringToNumber(String number) {
        if (Utils.isEmpty(number)) {
            return null;
        }
        return Integer.parseInt(number);
    }

    private static PackageInfo getPackageInfo(CallableStatement cs, ResultSet rsAppInfo) throws Exception {
        ResultSet rsPackage = (ResultSet) cs.getObject("P_PACKAGE");
        PackageInfo packageInfo = JdbcTransformer.toObject(rsPackage, PackageInfo.class);
        if (packageInfo == null) {
            String rawPackgeInfo = rsAppInfo.getString("FULL_PACKAGE_RAW");
            if (Utils.isEmpty(rawPackgeInfo)) {
                return null;
            }
            packageInfo = Json.decodeValue(rawPackgeInfo, PackageInfo.class);
        }
        packageInfo.setLenderLogic(Utils.nullToEmpty(rsAppInfo.getString("LENDER_LOGIC")));
        return packageInfo;
    }

    private static List<ReferencePeople> getReferencePeople(CallableStatement cs) throws SQLException {
        ResultSet rsReference = (ResultSet) cs.getObject("P_REFERENCE_PEOPLE");
        List<ReferencePeople> referencePeople = new ArrayList<>();
        while (rsReference.next()) {
            ReferencePeople referencePersonInfo = new ReferencePeople();
            referencePersonInfo.setFullName(rsReference.getString(Constant.FULL_NAME));
            referencePersonInfo.setPhoneNumber(rsReference.getString("PHONE_NUMBER"));
            KeyValue relationship = new KeyValue();
            relationship.setId(convertStringToNumber(rsReference.getString("CODE")));
            relationship.setName(rsReference.getString("RELATIVE"));
            referencePersonInfo.setRelationship(relationship);
            referencePersonInfo.setNationalId(rsReference.getString("NATIONAL_ID"));
            referencePeople.add(referencePersonInfo);
        }
        return referencePeople;
    }

    private static Map<ContractType, FileContractLink> getFileContractLinkData(CallableStatement cs) throws Exception {
        ResultSet rsContractFileLink = (ResultSet) cs.getObject("P_CONTRACT_FILE_LINK");
        Map<ContractType, FileContractLink> fileContractLinkMap = new HashMap<>();
        JdbcTransformer.toObjects(rsContractFileLink, FileContractLink.class, fileContractLink -> {
            fileContractLinkMap.put(ContractType.getByType(fileContractLink.getFileType()), fileContractLink);
        });
        return fileContractLinkMap;
    }


}
