package com.mservice.fs.onboarding.model.pendingform;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import com.mservice.fs.onboarding.model.status.PackageCache;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/11/2023
 */
@Getter
@Setter
public class PendingData {

    private CacheData pendingFormCache;
    private KnockOutRuleResponse knockOutRuleResponse;
    private PackageCache packageCache;

}
