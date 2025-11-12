package com.mservice.fs.onboarding.model.partnerroutingresult;

import com.mservice.fs.generic.validate.Validate;
import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.model.ai.LenderId;
import com.mservice.fs.onboarding.model.common.ai.LoanAction;
import com.mservice.fs.onboarding.model.common.ai.LoanRule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
@Getter
@Setter
public class LoanDeciderRecord {
    @Validate(notEmpty = true)
    private String agentId;
    @Validate(notEmpty = true)
    private String loanProductCode;
    private AIStatus loanDeciderStatus;
    private List<LoanAction> loanAction;
    private LenderId lenderId;

    private List<LoanRule> loanRule;
    private String messageType;
    private AIExpInfo expInfo;
    private String productGroup;
    private String productId;
    private String momoLoanId;
}
