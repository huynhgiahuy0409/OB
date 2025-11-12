package com.mservice.fs.onboarding.model.application.init;

import com.mservice.fs.generic.jdbc.DBColumn;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/17/2023
 */
@Getter
@Setter
public class ApplicationDataInit {

    @DBColumn(name = "CONTRACT_ID")
    private String contractId;
    @DBColumn(name = "REFERENCE_ID")
    private String referenceId;
    @DBColumn(name = "NEW_STATUS")
    private ApplicationStatus status;
    @DBColumn(name = "SERVICE_CODE")
    private String serviceId;

}
