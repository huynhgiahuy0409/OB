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
public class ApplicationDB {

    @DBColumn(name = "P_CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "P_REFERENCE_ID")
    private String referenceId;
    @DBColumn(name = "P_AGENT_ID")
    private String agentId;
    @DBColumn(name = "P_PHONE_NUMBER")
    private String phoneNumber;
    @DBColumn(name = "P_PARTNER_CODE")
    private String partnerId;
    @DBColumn(name = "P_SERVICE_CODE")
    private String serviceId;
    @DBColumn(name = "P_STATUS")
    private String status;
    @DBColumn(name = "P_STATE")
    private String state;
    @DBColumn(name = "P_MOMO_CREDIT_SCORE")
    private Double momoCreditScore;
    @DBColumn(name = "P_TAX_CODE")
    private String taxCode;
    @DBColumn(name = "P_INITIATOR")
    private String initiator;
    @DBColumn(name = "P_PARTNER_APPLICATION_ID")
    private String partnerApplicationId;
    @DBColumn(name = "P_REASON_ID")
    private int reasonId;
    @DBColumn(name = "P_REASON_MESSAGE")
    private String reasonMessage;
    @DBColumn(name = "P_PAYMENT_MERCHANT_INFO")
    private String paymentMerchantInfo;
    @DBColumn(name = "P_SOCIAL_SELLER_PROFILE")
    private String socialSellerProfile;
    @DBColumn(name = "P_TELCO")
    private Integer telco;

}
