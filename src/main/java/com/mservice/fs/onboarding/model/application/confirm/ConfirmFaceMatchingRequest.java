package com.mservice.fs.onboarding.model.application.confirm;

import com.mservice.fs.generic.validate.Validate;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/9/2024
 */
@Getter
@Setter
public class ConfirmFaceMatchingRequest extends ConfirmRequest {

    @Validate(notEmpty = true)
    private KycResult kycResult;
}
