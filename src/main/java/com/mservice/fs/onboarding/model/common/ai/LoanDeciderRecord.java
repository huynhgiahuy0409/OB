package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.onboarding.enums.AIStatus;
import com.mservice.fs.onboarding.model.LoanDeciderData;
import com.mservice.fs.onboarding.model.MerchantProfile;
import com.mservice.fs.utils.Utils;
import lombok.Getter;

import java.util.List;

/**
 * @author hoang.thai
 * on 8/28/2023
 */
@Getter
public class LoanDeciderRecord {

    private AIStatus loanDeciderStatus;
    private String momoLoanAppId;
    private List<LoanRule> loanRule;
    private List<LoanAction> loanAction;
    private String lenderId;
    private String messageType;
    private LoanDeciderData.Profile profile;
    private MerchantProfile merchantProfile;
    private String experimentTag;
    private Double momoCreditScore;

    public static LoanAction findFirstLoanAction(LoanDeciderRecord loanDeciderRecord) throws Exception {
        List<LoanAction> loanActions = loanDeciderRecord.getLoanAction();
        if (Utils.isEmpty(loanActions)) {
            return null;
        }
        return loanActions.getFirst();
    }

    public boolean isEmptyLoanAction() {
        return Utils.isEmpty(this.loanAction);
    }

}
