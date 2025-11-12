package com.mservice.fs.onboarding.model.application.update;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 5/23/2025
 **/
@Getter
@Setter
@Builder
public class UpdatePackageDB {
    @DBColumn(name = "P_APPLICATION_ID")
    private String applicationId;
    @DBColumn(name = "P_TENOR")
    private Integer tenor;
    @DBColumn(name = "P_LOAN_AMOUNT")
    private Long loanAmount;
    @DBColumn(name = "P_EMI")
    private Long emi;
    @DBColumn(name = "P_INCOME")
    private Long income;
    @DBColumn(name = "P_MONTHLY_INTEREST_RATE")
    private String monthlyInterestRate;
    @DBColumn(name = "P_EMAIL")
    private String email;
    @DBColumn(name = "P_REASON_MESSAGE")
    private String reasonMessage;
    @DBColumn(name = "P_PARTNER_APPLICATION_ID")
    private String partnerApplicationId;
}
