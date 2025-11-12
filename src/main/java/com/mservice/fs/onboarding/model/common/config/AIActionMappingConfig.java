package com.mservice.fs.onboarding.model.common.config;


import com.mservice.fs.json.Json;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AIActionMappingConfig {

    private String serviceId;
    private List<String> userProfileInfos = new ArrayList<>();
    private List<String> aiLoanActionNames = new ArrayList<>();

    @Override
    public String toString() {
        return Json.encode(this);
    }
}