package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.onboarding.model.application.UserProfileAI;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 9/12/2023
 */
@Getter
@Setter
public class GetPackageRequest {

    private String requestId;
    private String appSessionId;
    private String requestTimestamp;
    private String productGroup;
    private String merchantId;
    private String loanProductCode;
    private String productId;
    private int agentId;
    private String nationalId;
    private String sourceId;
    private UserProfileAI userProfile;
    private String offerPackageType;
    private String lenderId;
    private DeviceInfoAI deviceInfo;
    private String segmentUser;
}
