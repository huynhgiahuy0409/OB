package com.mservice.fs.onboarding.model.common;


import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.model.DefaultRequest;
import lombok.Getter;

@Getter
public class OnboardingBaseRequest extends DefaultRequest {

    @Validate(notEmpty = true)
    private String loanId;
    @Validate(notEmpty = true)
    private String partnerId;
    @Validate(notEmpty = true)
    private String serviceId;
    private Integer applicationStep;

}
