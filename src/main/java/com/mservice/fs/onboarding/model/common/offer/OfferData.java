package com.mservice.fs.onboarding.model.common.offer;

import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/10/2023
 */
@Getter
@Setter
public class OfferData {

    private KnockOutRuleResponse knockOutRuleResponse;
    private PackageCache packageCache;

}
