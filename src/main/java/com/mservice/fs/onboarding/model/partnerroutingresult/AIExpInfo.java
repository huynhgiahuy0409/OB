package com.mservice.fs.onboarding.model.partnerroutingresult;

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
public class AIExpInfo {
    private String expNamespace;
    private List<LoanRule> expRule;
}
