package com.mservice.fs.onboarding.model;

import com.mservice.fs.generic.validate.Validate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest extends OnboardingRequest {

    @Validate(notEmpty = true)
    private String partnerId;
    @Validate(notEmpty = true)
    private String applicationId;
}
