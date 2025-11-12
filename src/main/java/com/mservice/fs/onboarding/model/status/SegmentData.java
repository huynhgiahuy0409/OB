package com.mservice.fs.onboarding.model.status;

import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.api.status.WaitRoutingForm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hoang.thai
 * on 12/29/2023
 */
@Getter
@Setter
public class SegmentData {

    private UserType userType;
    private List<ApplicationDataLite> submittedForms = new ArrayList<>();
    private List<ApplicationDataLite> activeFormsForms;
    private List<ApplicationDataLite> closedForms;
    private List<WaitRoutingForm> waitRoutingForms;
    List<ApplicationForm> bannedStateForms;
    List<ApplicationForm> lockStateForms;
    private List<ApplicationDataLite> pendingStateForms;

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
