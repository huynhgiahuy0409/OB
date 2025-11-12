package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hoang.thai
 * on 12/5/2023
 */
@Getter
@Setter
public class ActionInfo {

    private Action action;
    private String processName;
    private List<String> allowFields;
    public ActionInfo() {
        allowFields = new ArrayList<>();
    }
}
