package com.mservice.fs.onboarding.model.verifyotp;

import com.mservice.fs.onboarding.model.OtpRequest;
import lombok.Getter;

@Getter
public class VerifyOtpRequest extends OtpRequest {

    private String otp;
    private String resultUrl;
}
