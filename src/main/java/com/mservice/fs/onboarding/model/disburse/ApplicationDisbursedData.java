package com.mservice.fs.onboarding.model.disburse;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ApplicationDisbursedData {
    @DBColumn(name = "REFERENCE_ID")
    private String applicationId;
    @DBColumn(name = "LOAN_AMOUNT")
    private long loanAmount;
    @DBColumn(name = "CREATE_TIME")
    private Timestamp createTime;
}
