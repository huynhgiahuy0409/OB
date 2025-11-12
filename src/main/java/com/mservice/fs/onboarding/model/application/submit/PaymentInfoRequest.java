package com.mservice.fs.onboarding.model.application.submit;

import lombok.Builder;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/22/2023
 */
@Setter
@Builder
public class PaymentInfoRequest {

    private String requestId;
    private long requestTimestamp;
    private long agentId;
    private String messageType;
    private String loanProductCode;
    private String partnerId;
    private String dataLogType;
    private String platformId;
    private String orderId;
    private String requestSource;
}
