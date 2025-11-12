package com.mservice.fs.onboarding;

import com.mservice.fs.starter.FsApp;

public class Starter extends FsApp {
    
    public static final String MODULE_NAME = "onboarding-platform";
    public static void main(String[] args) {
        (new Starter()).run(args, MODULE_NAME);
    }
    
}
