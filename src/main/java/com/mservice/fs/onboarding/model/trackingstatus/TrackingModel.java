package com.mservice.fs.onboarding.model.trackingstatus;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingModel {
    @DBColumn(name = "P_APPLICATION_ID")
    private String applicationId;
    @DBColumn(name = "P_PHONE_NUMBER")
    private String phoneNumber;
    @DBColumn(name = "P_AGENT_ID")
    private String agentId;
    @DBColumn(name = "P_PARTNER_ID")
    private String partnerId;
    @DBColumn(name = "P_SERVICE_ID")
    private String serviceId;
    @DBColumn(name = "P_STATUS")
    private String status;
    @DBColumn(name = "P_RAW_REQUEST")
    private String rawRequest;
    @DBColumn(name = "P_RAW_RESPONSE")
    private String rawResponse;
    @DBColumn(name = "P_RESULT_CODE")
    private Integer resultCode;
    @DBColumn(name = "P_PROCESS_NAME")
    private String processName;
    @DBColumn(name = "P_TRACE_ID")
    private String traceId;

}
