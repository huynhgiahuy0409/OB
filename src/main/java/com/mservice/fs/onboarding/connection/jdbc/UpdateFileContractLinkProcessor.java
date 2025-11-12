package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.BatchDbProcessor;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileContractLink;

import java.util.Map;

public class UpdateFileContractLinkProcessor extends BatchDbProcessor {

    public void execute(ApplicationData applicationData) throws Exception {

        Map<ContractType, FileContractLink> fileContractLinkMap = applicationData.getFileContractData();
        for (Map.Entry<ContractType, FileContractLink> entry : fileContractLinkMap.entrySet()) {
            FileContractLink fileContractLink = entry.getValue();
            String phoneNumber = applicationData.getPhoneNumber();
            String partnerId = applicationData.getPartnerId();
            long expiredTime = fileContractLink.getExpiredTime();
            String linkFile = fileContractLink.getLink();
            String pathFile = fileContractLink.getPath();
            String type = fileContractLink.getFileType();

            addBatch(false,
                    new CallableInputParam("P_REFERENCE_ID", applicationData.getApplicationId()),
                    new CallableInputParam("P_WALLET_ID", phoneNumber),
                    new CallableInputParam("P_PARTNER_ID", partnerId),
                    new CallableInputParam("P_FILE_EXPIRE_TIME", expiredTime),
                    new CallableInputParam("P_FILE_LINK", linkFile),
                    new CallableInputParam("P_FILE_PATH", pathFile),
                    new CallableInputParam("P_FILE_TYPE", type));
        }
        executeImmediately();
    }


    @Override
    protected boolean isAtomic() {
        return true;
    }

}
