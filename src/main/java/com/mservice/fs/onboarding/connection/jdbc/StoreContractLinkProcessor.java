package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.BatchDbProcessor;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileVersion;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractResponse;

public class StoreContractLinkProcessor extends BatchDbProcessor {

    public void execute() throws Exception {
        executeImmediately();
    }

    public void addContractBatch(ApplicationData applicationData, QueueContractResponse.AttachFileData attachFileData, ContractType contractType) throws Exception {
        String phoneNumber = applicationData.getPhoneNumber();
        String partnerId = applicationData.getPartnerId();
        String fileName = attachFileData.getName();
        long expiredTime = attachFileData.getExpiredTime();
        String linkFile = attachFileData.getLink();
        String pathFile = attachFileData.getPath();
        String type = contractType.getType();
        String version = FileVersion.NEW.name();

        addBatch(false,
                new CallableInputParam("P_REFERENCE_ID", applicationData.getApplicationId()),
                new CallableInputParam("P_WALLET_ID", phoneNumber),
                new CallableInputParam("P_PARTNER_ID", partnerId),
                new CallableInputParam("P_FILE_NAME", fileName),
                new CallableInputParam("P_FILE_EXPIRE_TIME", expiredTime),
                new CallableInputParam("P_FILE_LINK", linkFile),
                new CallableInputParam("P_FILE_PATH", pathFile),
                new CallableInputParam("P_FILE_TYPE", type),
                new CallableInputParam("P_VERSION", version));

    }

    @Override
    protected boolean isAtomic() {
        return true;
    }

}
