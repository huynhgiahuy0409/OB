package com.mservice.fs.onboarding.job.contract.store.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.StoreContractLinkProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.contract.store.task.StoreContractQueueTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;

import java.util.List;

public class StoreDBListener extends OnboardingListener<StoreContractRequest, StoreContractResponse> {

    private static final String NAME = "STORE_DB_CONTRACT";

    @Autowire(name = "StoreContractLink")
    private StoreContractLinkProcessor storeContractLinkProcessor;

    public StoreDBListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws Throwable {
        if (CommonErrorCode.SUCCESS.getCode().equals(onboardingData.getResponse().getResultCode())) {
            ApplicationData applicationData = onboardingData.getRequest().getApplicationData();
            Log.MAIN.info("Save Contract Link for applicationId {}", applicationData.getApplicationId());
            List<QueueContractResponse.AttachFileData> attachFileDatas = onboardingData.getTaskData(StoreContractQueueTask.NAME).getContent();
            OnboardingUtils.handleAttachFileData(attachFileDatas, ((attachFileData, contractType) -> {
                try {
                    storeContractLinkProcessor.addContractBatch(applicationData, attachFileData, contractType);
                } catch (Exception e) {
                    Log.MAIN.error("Error when execute store link file contract");
                    throw new RuntimeException(e);
                }

            }));
            storeContractLinkProcessor.execute();
        }
    }
}
