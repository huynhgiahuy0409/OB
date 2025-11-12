package com.mservice.fs.onboarding.model.telco;

import lombok.Getter;

/**
 * @author phat.duong
 * on 2/25/2025
 **/
@Getter
public class OtpResponse {
    private String requestId;
    private String otpStatus;
    private String partner;
    private String responseMessage;
    private String appSessionId;
    private String sla;
    private ExternalApiResponse externalApiResponse;
}
