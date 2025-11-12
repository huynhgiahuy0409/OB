package com.mservice.fs.onboarding.model.common.ai;

import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 4/18/2025
 **/
@Getter
@Setter
public class UserActionEvent {
    private String actionType;
    private String actionStatus;
    private long startTimestamp;
    private long endTimestamp;
    private String actionId;
}
