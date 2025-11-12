package com.mservice.fs.onboarding.model.contract;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreContractRequest extends OnboardingRequest {

    @Validate(notEmpty = true)
    private ApplicationData applicationData;
    private String otp;
}
