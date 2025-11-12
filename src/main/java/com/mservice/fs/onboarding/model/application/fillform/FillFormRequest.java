package com.mservice.fs.onboarding.model.application.fillform;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.ApplicationInfo;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Getter
public class FillFormRequest extends OnboardingRequest {

    @Validate(notEmpty = true)
    private String partnerId;
    @Validate(notEmpty = true)
    private String applicationId;
    @Validate(notEmpty = true)
    private ApplicationInfo applicationInfo;


}
