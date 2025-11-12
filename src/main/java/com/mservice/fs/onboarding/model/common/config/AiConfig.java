package com.mservice.fs.onboarding.model.common.config;

import static com.mservice.fs.onboarding.utils.OnboardingProcessor.*;

import com.mservice.fs.generic.jdbc.Transient;
import com.mservice.fs.generic.processor.Base;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author hoang.thai
 * on 12/3/2023
 */
@Getter
@Setter
public class AiConfig {

    @Transient
    private static final Set<String> PROCESS_NAME_QUICK = Set.of (
            CHECK_STATUS_QUICK, GET_PENDING_FORM_QUICK,
            INIT_APPLICATION_FORM_QUICK, INIT_CONFIRM_QUICK,
            CHECK_WHITE_LIST_PACKAGE
    );

    private String serviceId;
    private String loanProductCode;
    private String productId;
    private String productGroup;
    private Integer screenOrder;
    private String sourceId;
    private String sourceIdQuick;
    private String merchantId;
    private String platformId;
    private String loanDeciderPath;

    public String getSourceIdPackageFromBase(Base base) {
        return PROCESS_NAME_QUICK.contains(base.getProcessName()) ? sourceIdQuick : sourceId;
    }

    public boolean isQuickPackage(Base base) {
        return PROCESS_NAME_QUICK.contains(base.getProcessName());
    }
}
