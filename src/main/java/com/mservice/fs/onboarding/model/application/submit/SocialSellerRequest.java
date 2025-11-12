package com.mservice.fs.onboarding.model.application.submit;

import lombok.Builder;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/21/2023
 */
@Setter
@Builder
public class SocialSellerRequest {

    private String requestId;
    private long requestTimestamp;
    private IdentRequestLog identRequest;
}
