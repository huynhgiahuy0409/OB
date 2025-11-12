package com.mservice.fs.onboarding.model.application;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 9/5/2023
 */
@Getter
@Setter
public class UserProfileAI {

    private String userDob;
    private String userEmail;
    private String userName;
    private String nationalIdNumber;
    private String idCardType;
    private String expireDate;
    private String issueDate;
    private Integer gender;
    private String phoneNumber;
    private String taxCode;
    private String kycConfirmStatus;
    private String faceMatchingStatus;
    private String frontImagePath;
    private String backImagePath;
    private String issuePlace;
    private String address;
    private String kycC06Verified;
    private String latestKycTimestamp;
    private String latestFaceMatchingTimestamp;
    private String subBankCode;
    private NfcInfo nfcInfo;
    private C06Result c06Result;
    private Long walletCreatedTime;
    private String m4bFlag;
    private String merchantName;
    private boolean merchantAutoCashout;
    public UserProfileAI() {

    }

}
