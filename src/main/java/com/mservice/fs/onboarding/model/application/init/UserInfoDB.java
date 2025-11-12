package com.mservice.fs.onboarding.model.application.init;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/8/2023
 */
@Getter
@Setter
@Builder
public class UserInfoDB {

    @DBColumn(name = "P_REFERENCE_ID")
    private String referenceId;
    @DBColumn(name = "P_CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "P_FULL_NAME")
    private String fullName;
    @DBColumn(name = "P_GENDER")
    private String gender;
    @DBColumn(name = "P_DOB")
    private String dob;
    @DBColumn(name = "P_EMAIL")
    private String email;
    @DBColumn(name = "P_NATIONALITY")
    private String nationality;
    @DBColumn(name = "P_TAX_CODE")
    private String taxCode;
    @DBColumn(name = "P_PERSONAL_ID")
    private String personalId;
    @DBColumn(name = "P_MONTHLY_INCOME")
    private Long income;
    @DBColumn(name = "P_ID_ISSUE_DATE")
    private String issueDate;
    @DBColumn(name = "P_ID_ISSUE_PLACE")
    private String issuePlace;
    @DBColumn(name = "P_ID_TYPE")
    private String idType;
    @DBColumn(name = "P_EXPIRY_DATE")
    private String expiryDate;
    @DBColumn(name = "P_PARTNER_CODE")
    private String partnerId;
    @DBColumn(name = "P_MODIFIED_NAME")
    private String modifiedName;
    @DBColumn(name = "P_FRONT_IMAGE_PATH")
    private String frontImagePath;
    @DBColumn(name = "P_BACK_IMAGE_PATH")
    private String backImagePath;
    @DBColumn(name = "P_FACE_MATCHING_IMAGE_PATH")
    private String faceMatchingImagePath;
    @DBColumn(name = "P_FRONT_IMAGE_URL")
    private String frontImageUrl;
    @DBColumn(name = "P_BACK_IMAGE_URL")
    private String backImageUrl;
    @DBColumn(name = "P_FACE_MATCHING_IMAGE_URL")
    private String faceMatchingImageUrl;

}
