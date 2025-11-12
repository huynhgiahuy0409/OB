package com.mservice.fs.onboarding.model.common.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.json.Json;
import com.mservice.fs.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 8/15/2023
 */

@Getter
@Setter
public class LoanAction {

    private String actionId;
    private String actionName;
    private int actionOrder;
    private String actionValue;

    public String encode() throws JsonProcessingException {
        return JsonUtil.toString(this);
    }

    @Override
    public String toString() {
        return Json.encodeHiddenFields(this);
    }
}
