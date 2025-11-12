package com.mservice.fs.onboarding.model.application.submit;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.ApplicationInfo;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import lombok.Getter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Getter
public class SubmitRequest extends ConfirmRequest {

    @Validate(notEmpty = true)
    private ApplicationInfo applicationInfo;

}
