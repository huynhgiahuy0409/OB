package com.mservice.fs.onboarding.model.initconfirm;

import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
@Getter
@Setter
public class InitConfirmResponse extends OnboardingResponse {

    private String redirectTo;
    private ApplicationData applicationData;

}
