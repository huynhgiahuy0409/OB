package com.mservice.fs.onboarding.job.crm.listener;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.connection.jdbc.StoreDeletedCacheProcessor;
import com.mservice.fs.onboarding.job.crm.task.UpdateCacheTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.onboarding.model.crm.CrmUpdateScamRequest;
import com.mservice.fs.onboarding.model.crm.LoanListWrapper;
import com.mservice.fs.processor.AbstractListener;

import java.util.List;

public class InsertTrackingUpdateListener extends AbstractListener<PlatformData<CrmUpdateScamRequest, CrmResponse>, CrmUpdateScamRequest, CrmResponse, OnboardingConfig> {
    public InsertTrackingUpdateListener() {
        super("INSERT_TRACKING");
    }

    @Autowire
    private StoreDeletedCacheProcessor storeDeletedCacheProcessor;

    @Override
    public void execute(PlatformData<CrmUpdateScamRequest, CrmResponse> platformData) throws Throwable {
        //create proc, create table to save application_id, service_id, phone_number, form(varchar2 size 4000), cancel_by, reason;
        if (platformData.getResult().getCode().equals(CommonErrorCode.Code.SUCCESS)) {
            LoanListWrapper loanListWrapper = platformData.getTaskData(UpdateCacheTask.NAME).getContent();
            List<ApplicationData> updateList = loanListWrapper.getUpdateList();
            if (!updateList.isEmpty()) {
                for (ApplicationData applicationData : updateList) {
                    storeDeletedCacheProcessor.execute(applicationData, platformData.getRequest());
                }
            }
        }
    }
}
