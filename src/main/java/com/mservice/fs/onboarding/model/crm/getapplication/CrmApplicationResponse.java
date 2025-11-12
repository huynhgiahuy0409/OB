package com.mservice.fs.onboarding.model.crm.getapplication;

import com.mservice.fs.onboarding.model.OnboardingResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CrmApplicationResponse extends OnboardingResponse {
    private String requestId;
    private List<CrmApplicationInfo> loanInfo = new ArrayList<>();
}
