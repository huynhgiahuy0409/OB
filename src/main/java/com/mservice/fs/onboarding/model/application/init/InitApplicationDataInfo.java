package com.mservice.fs.onboarding.model.application.init;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/23/2023
 */
@Getter
@Setter
public class InitApplicationDataInfo {

    private CacheData cachePackage;
    private KnockOutRuleResponse knockOutRuleResponse;
    private List<ApplicationForm> pendingForms;

}
