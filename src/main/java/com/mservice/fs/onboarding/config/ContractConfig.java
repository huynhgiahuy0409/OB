package com.mservice.fs.onboarding.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/19/2024
 */
@Getter
@Setter
public class ContractConfig {

    private boolean active;
    private long expirationPeriod;
    private AttachFileConfig storeFilePDF;
    private AttachFileConfig genLinkPDF;
}
