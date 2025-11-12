package com.mservice.fs.onboarding.model.crm.getapplication;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;

@Getter
public class CrmApplicationRequest extends OnboardingRequest{
    @Validate(notEmpty = true)
    private String serviceId;
    private String phoneNumber;
    private String loanId;
    private String beginDate;
    private String endDate;
}
