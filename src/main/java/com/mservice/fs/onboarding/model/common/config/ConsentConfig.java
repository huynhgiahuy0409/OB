package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.model.consent.ConsentRequest;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConsentConfig {

    private static final String _CLASS = "mservice.backend.entity.msg.IamMsg";
    private List<ConsentRequest.Attribute> attributeList;
    private String miniAppId;
    private String partnerCode;

    public ConsentConfig() {
        attributeList = new ArrayList<>();
    }

    public void addAttribute(String attributeName, String accessType, String miniAppId, String serviceId, String partnerCode) {
        if (Utils.isEmpty(attributeName) || Utils.isEmpty(accessType) || Utils.isEmpty(miniAppId)) {
            Log.MAIN.error("Can not add attribute for serviceId: {} with accessType: {} - attributeName: {} - miniAppId: {} ", serviceId, accessType, attributeName, miniAppId);
            return;
        }
        ConsentRequest.Attribute attribute = new ConsentRequest.Attribute();
        attribute.setAttribute(attributeName);
        attribute.setAccessType(accessType);
        this.attributeList.add(attribute);
        this.miniAppId = miniAppId;
        this.partnerCode = partnerCode;
    }
}
