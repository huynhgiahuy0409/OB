package com.mservice.fs.onboarding.model.partnerroutingresult;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
@Getter
@Setter
public class PartnerRoutingRequest extends OnboardingRequest {
    private String appSessionId;
    private String errorMessage;
    private String responseTimestamp;
    private String modelVersion;
    @Validate(notEmpty = true, checkObject = true)
    private LoanDeciderRecord loanDeciderRecord;
    private String sourceId;
}
