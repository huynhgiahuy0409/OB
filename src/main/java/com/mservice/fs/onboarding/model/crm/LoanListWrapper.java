package com.mservice.fs.onboarding.model.crm;

import com.mservice.fs.onboarding.model.ApplicationData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoanListWrapper {

    private List<LoanInfo> fetchList;

    private List<LoanInfo> removeList;
    private List<ApplicationData> updateList;
}

