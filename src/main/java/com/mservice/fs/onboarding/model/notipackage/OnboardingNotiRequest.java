package com.mservice.fs.onboarding.model.notipackage;


import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Data;

@Data
public class OnboardingNotiRequest extends OnboardingRequest {

    private final String lang = "vi";
    @Validate(notNull = true)
    private String agentId;
}
