package com.mservice.fs.onboarding.model.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ConsentRequest {

    private MomoMsg momoMsg;
    private long time;
    private String user;
    private String cmdId;
    private String msgType;
    private String channel = "BACKEND";

    public ConsentRequest(MomoMsg momoMsg, String msgType, String user, long time, String cmdId) {
        this.momoMsg = momoMsg;
        this.time = time;
        this.user = user;
        this.cmdId = cmdId;
        this.msgType = msgType;
    }

    @Getter
    @Setter
    public static class MomoMsg {

        private String requestId;
        private String miniAppId;
        private List<Attribute> attributeList;
        @JsonProperty("_class")
        private String clazz = "mservice.backend.entity.msg.IamMsg";
    }

    @Getter
    @Setter
    public static class Attribute {
        private String id;
        private String attribute;
        private String displayName;
        private String displayVi;
        private String accessType;
        private Boolean required;
        private Object value;
    }

}
