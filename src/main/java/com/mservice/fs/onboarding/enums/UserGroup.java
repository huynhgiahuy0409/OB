package com.mservice.fs.onboarding.enums;

/**
 * User's risk level.
 */
public enum UserGroup {
    GROUP_A("GROUP_A"),

    /**
     * User profile missing some fields/
     */
    GROUP_B("GROUP_B");

    private final String code;

    UserGroup(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
