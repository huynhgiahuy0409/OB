package com.mservice.fs.onboarding.job.crm.listener;

import com.google.api.gax.rpc.StatusCode;
import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.connection.jdbc.StoreDeletedCacheProcessor;
import com.mservice.fs.onboarding.job.crm.task.GetCacheTask;
import com.mservice.fs.onboarding.job.crm.task.RemoveCacheTask;
import com.mservice.fs.onboarding.model.crm.CrmCancelApplicationRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.onboarding.model.crm.LoanInfo;
import com.mservice.fs.onboarding.model.crm.LoanListWrapper;
import com.mservice.fs.processor.AbstractListener;
import org.apache.kafka.common.utils.CollectionUtils;

import java.util.List;

public class InsertTrackingListener extends AbstractListener<PlatformData<CrmCancelApplicationRequest, CrmResponse>,CrmCancelApplicationRequest, CrmResponse, OnboardingConfig> {
    public InsertTrackingListener() {
        super("INSERT_TRACKING");
    }

    @Autowire
    private StoreDeletedCacheProcessor storeDeletedCacheProcessor;

    @Override
    public void execute(PlatformData<CrmCancelApplicationRequest, CrmResponse> platformData) throws Throwable {
        //create proc, create table to save application_id, service_id, phone_number, form(varchar2 size 4000), cancel_by, reason;
        if (platformData.getResult().getCode().equals(CommonErrorCode.Code.SUCCESS)) {
            LoanListWrapper loanListWrapper = platformData.getTaskData(RemoveCacheTask.NAME).getContent();
            List<LoanInfo> removeList = loanListWrapper.getRemoveList();
            if (!removeList.isEmpty()) {
                for (LoanInfo loanInfo : removeList) {
                    storeDeletedCacheProcessor.execute(loanInfo, platformData.getRequest());
                }
            }
        }
    }
}
