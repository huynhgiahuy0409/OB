package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.onboarding.model.application.KycDataAI;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 8/14/2023
 */

@Getter
@Setter
public class SummaryScreenLog {
    private String agentId;
    private String screenId;
    private String selectedLoanPackage;
    private long timestamp;
    private String loanProductCode;
    private String productId;
    private String momoLoanAppId;
    private String miniAppVersion;
    private Profile profile;
    private KycDataAI kycData;
    private int screenOrder;
    private String userGroup;
    private Double momoCreditScore;
    private CashLoanItem cashLoanItem;
    private PaylaterLoanItem paylaterLoanItem;
    private OrderInfo orderInfo;
    private String lenderId;

    @Getter
    @Setter
    public static class CashLoanItem {
        private long loanAmount;
    }

    @Getter
    @Setter
    public static class PaylaterLoanItem {
        private long loanAmount;
    }

    @Getter
    @Setter
    public static class OrderInfo {
        private long totalOrderAmount;
        private long totalLoanAmount;
    }

    @Getter
    @Setter
    public static class Profile {
        private String userName;
        private String userEmail;
        private String address;
        private String userDob;
        private String issueDate;
        private String nationalIdNumber;
        private long userIncome;
        private int idCardType;
        private String expireDate;
        private String phoneNumber;
        private int gender;
        private String workingPlace;
        private String workingNumber;
        private String receivePlace;
        private List<RelativeInfo> relativesInfo;
        private String taxCode;
        private String kycConfirmStatus;
        private String faceMatchingStatus;
        private String frontImagePath;
        private String backImagePath;
        private List<UserActionEvent> userActionEvent;
        private Long walletCreatedTime;
        private String kycC06Verified;
        private String subBankCode;
        private String m4bFlag;
        private String merchantName;
        private boolean merchantAutoCashout;

        public Profile(){

        }
    }

    @Setter
    @Getter
    public static class RelativeInfo {

        public RelativeInfo(){

        }
        private String relativeName;
        private String relationship;
        private String relativePhoneNumber;
    }

    public static enum Gender {
        UNKNOWN_GENDER(0),
        MALE(1),
        FEMALE(2);

        private final int code;

        Gender(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static enum IdCardType {
        UNKNOWN_ID_CARD_TYPE(0),
        CMND(1),
        CCCD(2),
        PASSPORT(3),
        OTHER_TYPE(4),
        CHIP(5);

        private final int code;

        IdCardType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
