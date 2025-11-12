package com.mservice.fs.onboarding.model.initconfirm;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
@Getter
@Setter
public class InitConfirmRequest extends OnboardingRequest {

    @Validate(notEmpty = true)
    private String partnerId;
    @Validate(notEmpty = true)
    private String applicationId;
}
