package com.mservice.fs.onboarding.config;

import com.mservice.fs.jdbc.processor.MultiConfig;
import lombok.Getter;

/**
 * @author hoang.thai
 * on 11/8/2023
 */
@Getter
public class MultiStoreConfig extends MultiConfig {

    private String storeApplicationInfo;
    private String storeAddressInfo;
    private String storeUser;
    private String storeRelativeInfo;
    private String storePackageInfo;
    private String storeAdditionalData;
}
