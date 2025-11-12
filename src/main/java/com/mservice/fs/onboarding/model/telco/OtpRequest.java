package com.mservice.fs.onboarding.model.telco;

import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 2/25/2025
 **/
@Getter
@Setter
public class OtpRequest {
    private String requestId;
    private String phoneNumber;
    private String telcoCourier;
    private String appSessionId;
    private String requestSource;
    private String otp;
    private String partner;
}
