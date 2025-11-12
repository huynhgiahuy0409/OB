package com.mservice.fs.onboarding.model.crm;

import com.mservice.fs.generic.validate.Validate;
import lombok.Getter;

@Getter
public class CrmCancelApplicationRequest extends CrmListRequest {
    private String loanId;
    private String serviceId;
    private String partnerId;
    @Validate(notEmpty = true)
    private String cancelBy;
    private String reason;
}
