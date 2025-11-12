package com.mservice.fs.onboarding.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.model.ai.ScreenId;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 9/5/2023
 */
@Getter
@Setter
public class ScreenLog {

    private String miniAppVersion;
    private ScreenId screenId;
    private int screenOrder;
    @JsonProperty("loanAppStatus")
    private int loanStatus;
    private int lendingFlow;
    private DeviceInfo deviceInfo;
    private UserProfileAI profile;
    private KycDataAI kycData;
    private long timestamp;
    private AIMessageType messageType;

}