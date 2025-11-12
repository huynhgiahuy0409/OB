package com.mservice.fs.onboarding.enums;

public enum PrefixOnboarding {

    NEW(1),
    OLD(0)
    ;

    private int code;

    private PrefixOnboarding(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }

}
