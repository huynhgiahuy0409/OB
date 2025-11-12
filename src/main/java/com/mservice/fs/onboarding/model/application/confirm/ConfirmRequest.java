package com.mservice.fs.onboarding.model.application.confirm;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.model.DeviceLocation;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.application.submit.ScamAlertResult;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
@Getter
@Setter
public class ConfirmRequest extends OnboardingRequest {

    @Validate(notEmpty = true)
    private String partnerId;
    @Validate(notEmpty = true)
    private String applicationId;
    private String deviceId;
    private String ipAddress;
    private DeviceLocation location;
    private ScamAlertResult scamAlertResult;
}
