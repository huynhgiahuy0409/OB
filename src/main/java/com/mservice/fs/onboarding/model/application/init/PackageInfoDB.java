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
public class PackageInfoDB {

    @DBColumn(name = "P_CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "P_PRODUCT_GROUP")
    private String productGroup;
    @DBColumn(name = "P_PACKAGE_GROUP")
    private String packageGroup;
    @DBColumn(name = "P_PACKAGE_NAME")
    private String packageName;
    @DBColumn(name = "P_PACKAGE_CODE")
    private String packageCode;
    @DBColumn(name = "P_LENDER_ID")
    private String lenderId;
    @DBColumn(name = "P_RANK")
    private Integer rank;
    @DBColumn(name = "P_TENOR")
    private Integer tenor;
    @DBColumn(name = "P_LOAN_AMOUNT")
    private Long loanAmount;
    @DBColumn(name = "P_DISBURSED_AMOUNT")
    private Long disbursedAmount;
    @DBColumn(name = "P_INTEREST_AMOUNT")
    private Long interestAmount;
    @DBColumn(name = "P_INTEREST_UNIT")
    private String interestUnit;
    @DBColumn(name = "P_SERVICE_FEE")
    private Long serviceFee;
    @DBColumn(name = "P_COLLECTION_FEE")
    private Long collectionFee;
    @DBColumn(name = "P_DISBURSED_FEE")
    private Long disbursedFee;
    @DBColumn(name = "P_LATE_INTEREST")
    private Double lateInterest;
    @DBColumn(name = "P_LATE_FEE")
    private Long lateFee;
    @DBColumn(name = "P_INTEREST")
    private Double interest;
    @DBColumn(name = "P_PAYMENT_AMOUNT")
    private Long paymentAmount;
    @DBColumn(name = "P_EMI")
    private Long emi;
    @DBColumn(name = "P_TENOR_UNIT")
    private String tenorUnit;
    @DBColumn(name = "P_SEGMENT_USER")
    private String segmentUser;
    @DBColumn(name = "P_LENDER_LOGIC")
    private String lenderLogic;
    @DBColumn(name = "P_DUE_DAY")
    private String dueDay;
    @DBColumn(name = "P_PARTNER_ID")
    private String partnerId;
    @DBColumn(name = "P_PACKAGE_MAP_NAME")
    private String packageMapName;
    @DBColumn(name = "P_LENDER_NAME")
    private String lenderName;
}
