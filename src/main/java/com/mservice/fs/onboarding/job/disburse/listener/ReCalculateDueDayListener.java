package com.mservice.fs.onboarding.job.disburse.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.UpdateDueDayPackageProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 9/12/2025
 **/
public class ReCalculateDueDayListener extends OnboardingListener<OnboardingDisburseRequest, OnboardingDisburseResponse> {

    public static final String NAME = "RE_CALCULATE_DUE_DAY";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;
    @Autowire
    UpdateDueDayPackageProcessor updateDueDayPackageProcessor;

    public ReCalculateDueDayListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) throws Throwable {
        ApplicationData applicationData = onboardingData.getTaskData(UpdatingStatusTask.NAME).getContent();

        if (CommonErrorCode.SUCCESS.getCode().equals(onboardingData.getResponse().getResultCode())
                && Utils.isNotEmpty(applicationData) && Utils.isNotEmpty(applicationData.getChosenPackage())
                && getConfig().getServiceApplyRecalculateDueDate().contains(onboardingData.getServiceId())) {

            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
            String dueDay = OnboardingUtils.getDueDate(serviceObInfo, applicationData.getChosenPackage().getLenderId());
            Log.MAIN.info("Update dueDay from {} to {}", applicationData.getChosenPackage().getDueDay(), dueDay);
            updateDueDayPackageProcessor.execute(applicationData.getApplicationId(), dueDay);
        }

    }
}
