package com.mservice.fs.onboarding.model.application.submit;

import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Getter
@Setter
public class SubmitResponse extends OnboardingResponse {

    private ApplicationData applicationData;
    private int currentGenerateTimes;
    private int currentVerifyTimes;
    private int maxVerifyTimes;
    private int maxGenerateTimes;
    private long validOtpInMillis;
    private String applicationId;
    private String webViewLink;
    private int reasonId;
    private String reasonMessage;
}
