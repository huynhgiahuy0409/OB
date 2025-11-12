package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.model.common.config.AIActionMappingConfig;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class KnockoutRuleTracking {
    private String status;
    private boolean checkLoanAction;
    private boolean aiActionMapping;
    private String message;
    private List<LoanAction> loanDeciderRecords = new ArrayList<>();
    private List<LoanAction> loanActionNotConfig = new ArrayList<>();
    private AIActionMappingConfig aiActionMappingConfig;
    private int resultCode;

    public void activeAiActionMapping() {
        this.aiActionMapping = true;
    }

    public void activeCheckLoanAction() {
        this.checkLoanAction = true;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAiActionMappingConfig(AIActionMappingConfig aiActionMappingConfig) {
        this.aiActionMappingConfig = aiActionMappingConfig;
    }

    public void setLoanDeciderRecords(List<LoanAction> loanDeciderRecords) {
        if (Utils.isEmpty(loanDeciderRecords)) {
            return;
        }
        this.loanDeciderRecords = loanDeciderRecords;
    }

    public void setLoanActionNotConfig(List<LoanAction> loanActionNotConfig) {
        if (Utils.isEmpty(loanActionNotConfig)) {
            return;
        }
        this.loanActionNotConfig = loanActionNotConfig;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return Json.encode(this);
    }
}
