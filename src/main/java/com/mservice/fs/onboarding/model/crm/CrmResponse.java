package com.mservice.fs.onboarding.model.crm;

import com.mservice.fs.json.Json;
import com.mservice.fs.model.PlatformDefaultResponse;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CrmResponse extends PlatformDefaultResponse {
    private List<LoanInfo> loanInfo = new ArrayList<>();

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
