package com.mservice.fs.onboarding.model.application;

import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author muoi.nong
 */
@Getter
@Setter
public class ApplicationCheckStatusDbWrapper {

    private List<ApplicationDataInit> applicationDbValidDeDups;
    private List<ApplicationDataLite> applicationDataLites;
}
