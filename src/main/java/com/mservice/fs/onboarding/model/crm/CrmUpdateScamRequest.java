package com.mservice.fs.onboarding.model.crm;

import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 9/8/2025
 **/
@Getter
@Setter
public class CrmUpdateScamRequest extends CrmRequest {
    private String loanId;
    private String scamStatus;
}
