package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OBPartnerConfig {

    String serviceId;
    String partnerId;
    Integer miniAppTrackVerPackage;
    Integer applyStatusAtFinalSubmit;
    Integer applyKnockOutRuleLenderId;
    Integer applyScoreAtLoanDecider;
    String lenderIdAI;
    String callerId;
    Integer applySendPlatformListener;
    Integer applySendPlatformTask;
}
