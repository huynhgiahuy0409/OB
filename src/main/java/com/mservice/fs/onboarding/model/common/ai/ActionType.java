package com.mservice.fs.onboarding.model.common.ai;

import lombok.Getter;

/**
 * @author phat.duong
 * on 4/18/2025
 **/
@Getter
public enum ActionType {
    READ_TEXT("READ_TEXT", "ALERT_TEXT_1"),
    WATCH_VIDEO("WATCH_VIDEO", "SCAM_VIDEO_1"),
    UNKNOWN_ACTION_TYPE("UNKNOWN_ACTION_TYPE", "SCAM_INFO_1");

    ActionType(String type, String id) {
        this.type = type;
        this.id = id;
    }

    private String type;
    private String id;

    public static ActionType findByType(String type) {
        for (ActionType actionType : values()) {
            if (actionType.getType().equals(type)) {
                return actionType;
            }
        }
        return UNKNOWN_ACTION_TYPE;
    }

}
