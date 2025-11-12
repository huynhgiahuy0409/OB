package com.mservice.fs.onboarding.model.application.fillform;

import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Setter
@Getter
public class FillFormResponse extends OnboardingResponse {

    private ApplicationData applicationData;
}
