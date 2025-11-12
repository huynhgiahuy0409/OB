package com.mservice.fs.onboarding.model.status;

import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.common.ai.KnockOutRuleResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 12/7/2023
 */
@Getter
@Setter
public class CheckStatusData {

    private List<ApplicationForm> applicationForms;
    private KnockOutRuleResponse knockOutRuleResponse;
    private PackageCache packageCache;
    private ApplicationForm modifyApplicationForm;

    public boolean isSaveCache() {
        if (knockOutRuleResponse == null) {
            return false;
        }
        return true;
    }

}
