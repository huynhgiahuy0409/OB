package com.mservice.fs.onboarding.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeviceInfo {
    @JsonProperty("isNfcAvailable")
    private boolean nfcAvailable = false;
}
