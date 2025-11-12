package com.mservice.fs.onboarding.model.db;

import com.mservice.fs.generic.jdbc.DBColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
@Getter
@Setter
@Accessors(chain = true)
public class UpdateRoutingResultDB {

    @DBColumn(name = "P_AGENT_ID")
    private String agentId;
    @DBColumn(name = "P_PREVIOUS_ROUTING_STATUS")
    private String previousRoutingStatus;
    @DBColumn(name = "P_ROUTING_STATUS")
    private String routingStatus;
    @DBColumn(name = "P_SERVICE_ID")
    private String serviceId;
}
