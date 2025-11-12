package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hoang.thai
 * on 1/18/2024
 */
@Getter
@Setter
public class PartnerConfig {
    String id;
    Set<ContractType> typeContracts = new HashSet<>();
    OtpConfig otpConfig = new OtpConfig();
    Integer miniAppTrackVerPackage;
    boolean applyStatusAtFinalSubmit;
    boolean applyKnockOutRuleLenderId;
    boolean applyScoreAtLoanDecider;
    boolean applySendPlatformListener;
    String lenderIdAI;
    String callerId;
    boolean applySendPlatformTask;

    public void addTypeContract(String type, boolean applyZeroInterest) {
        typeContracts.add(new ContractType(type, applyZeroInterest));
    }

}
