package com.mservice.fs.onboarding.config;

import com.mservice.fs.jdbc.processor.MultiConfig;
import lombok.Getter;

/**
 * @author muoi.nong
 */
@Getter
public class MultiUpdateConfig extends MultiConfig {

    private String updateAddressInfo;
    private String updateRelativeInfo;
    private String updateAdditionalData;
    private String updatePackage;
}
