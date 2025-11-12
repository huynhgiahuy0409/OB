package com.mservice.fs.onboarding.model.common.ai;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Getter
@Setter
public class LoanDeciderResponse {

    private String responseTimestamp;
    private LoanDeciderRecord loanDeciderRecord;
}
