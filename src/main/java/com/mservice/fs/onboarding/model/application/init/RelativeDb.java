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
public class RelativeDb {

    @DBColumn(name = "P_REFERENCE_ID")
    private String referenceId;
    @DBColumn(name = "P_FULL_NAME")
    private String fullName;
    @DBColumn(name = "P_PHONE_NUMBER")
    private String phoneNumber;
    @DBColumn(name = "P_CODE")
    private Integer code;
    @DBColumn(name = "P_RELATIVE")
    private String relative;
    @DBColumn(name = "P_PARTNER_CODE")
    private String partnerCode;
    @DBColumn(name = "P_CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "P_NATIONAL_ID")
    private String nationalId;

}
