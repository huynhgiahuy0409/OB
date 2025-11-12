package com.mservice.fs.onboarding.model.db;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UpdatingStatusDB {

    @DBColumn(name = "P_APPLICATION_ID")
    private String applicationId;

    @DBColumn(name = "P_PHONE_NUMBER")
    private String phoneNumber;

    @DBColumn(name = "P_STATUS")
    private String status;

    @DBColumn(name = "P_STATE")
    private String state;

    @DBColumn(name = "P_SERVICE_ID")
    private String serviceId;

    @DBColumn(name = "P_PARTNER_ID")
    private String partnerId;

    @DBColumn(name = "P_RAW_REQUEST")
    private String rawRequest;

    @DBColumn(name = "P_PREVIOUS_STATUS")
    private String previousStatus;

    @DBColumn(name = "P_REASON_ID")
    private int reasonId;

    @DBColumn(name = "P_REASON_MESSAGE")
    private String reasonMessage;

    @DBColumn(name = "P_ROUTING_PACKAGE_STATUS")
    private String routingPackageStatus;
}
