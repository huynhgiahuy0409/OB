package com.mservice.fs.onboarding.model.application.init;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/8/2023
 */
@Getter
@Setter
public class AddressDB {

    @DBColumn(name = "P_REFERENCE_ID")
    private String referenceId;
    @DBColumn(name = "P_FULL_ADDRESS")
    private String fullAddress;
    @DBColumn(name = "P_STREET")
    private String street;
    @DBColumn(name = "P_WARD_CODE")
    private String wardCode;
    @DBColumn(name = "P_WARD_NAME")
    private String wardName;
    @DBColumn(name = "P_DISTRICT_CODE")
    private String districtCode;
    @DBColumn(name = "P_DISTRICT_NAME")
    private String districtName;
    @DBColumn(name = "P_PROVINCE_CODE")
    private String provinceCode;
    @DBColumn(name = "P_PROVINCE_NAME")
    private String provinceName;
    @DBColumn(name = "P_ADDRESS_TYPE")
    private String addressType;
    @DBColumn(name = "P_PARTNER_CODE")
    private String partnerCode;
    @DBColumn(name = "P_CONTRACT_ID")
    private String contractId;

}
