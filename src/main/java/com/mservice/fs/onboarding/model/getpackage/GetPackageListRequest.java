package com.mservice.fs.onboarding.model.getpackage;

import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPackageListRequest extends OnboardingRequest {
    private String loanProductCode;
}
