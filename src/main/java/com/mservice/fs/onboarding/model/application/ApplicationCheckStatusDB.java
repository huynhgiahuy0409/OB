package com.mservice.fs.onboarding.model.application;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
@Getter
@Setter
public class ApplicationCheckStatusDB {

    private String referenceId;
    private String status;
    private String state;
    private String newStatus;
    private String partnerCode;
    private String serviceCode;
    private Date createTime;
    private Date lastModified;
    private String contractId;
    private String packageGroup;
    private String packageName;
    private String packageCode;
    private String lenderId;
    private int rank;
    private int tenor;
    private long loanAmount;
    private long disbursedAmount;
    private long interestAmount;
    private String interestUnit;
    private long serviceFee;
    private long collectionFee;
    private long disbursedFee;
    private double lateInterest;
    private long lateFee;
    private double interest;
    private long paymentAmount;
    private long emi;
    private String tenorUnit;
    private String segmentUser;
    private String lenderLogic;
    private String productGroup;
    private String dueDay;
    private String fullName;
    private String modifiedName;
    private int reasonId;
    private String reasonMessage;
    private String fullPackageRaw;
    private long minLoanAmount;
    private long maxLoanAmount;
    private String routingPackageStatus;
}
