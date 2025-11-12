package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.listener.NotificationListener;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import lombok.SneakyThrows;

import java.util.Set;

public abstract class AbsNotiUserListener<T extends OnboardingRequest, R extends OnboardingResponse> extends NotificationListener<OnboardingData<T, R>, T, R, OnboardingConfig> {

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final Set<ApplicationStatus> STATUS_NOTI = Set.of(ApplicationStatus.VERIFIED_OTP_SUCCESS, ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    private static final String APPLICATION = "application";

    @SneakyThrows
    @Override
    protected boolean isActive(OnboardingData<T, R> jobData) {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.NOTI_USER, jobData.getProcessName())) {
            ApplicationForm applicationForm = getApplicationForm(jobData);
            if (applicationForm != null) {
                ApplicationData applicationData = applicationForm.getApplicationData();
                ApplicationStatus status = applicationData.getStatus();
                if (STATUS_NOTI.contains(status)) {
                    ApplicationDataLite applicationDataLite = new ApplicationDataLite();
                    applicationDataLite.setApplicationId(applicationData.getApplicationId());
                    applicationDataLite.setChosenPackage(applicationData.getChosenPackage());
                    jobData.getTemplateModel().put(APPLICATION, applicationDataLite);
                    Log.MAIN.info("ApplicationData not null, active listener !!! ");
                    return true;
                }
                Log.MAIN.info("Status [{}] not in list status [{}]", status, STATUS_NOTI);
            }
        }
        return false;
    }

    @Override
    protected String getTemplateCondition(OnboardingData<T, R> jobData, T request, R response) {
        return getApplicationForm(jobData).getApplicationData().getStatus().name();
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T,R> jobData);
}
