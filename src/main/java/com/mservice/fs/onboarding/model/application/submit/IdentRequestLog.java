package com.mservice.fs.onboarding.model.application.submit;

import lombok.Builder;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/21/2023
 */
@Setter
@Builder
public class IdentRequestLog {

    private String loanProductCode;
    private String productId;
    private String productGroup;
    private String partnerId;
    private String agentId;
    private String nationalId;
    private String phoneNumber;

}
