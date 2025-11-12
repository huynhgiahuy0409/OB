package com.mservice.fs.onboarding.job.contract.store.task;

import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileContractLink;
import com.mservice.fs.onboarding.model.FileVersion;
import com.mservice.fs.onboarding.model.api.otp.verify.GenContractRequest;
import com.mservice.fs.onboarding.model.api.otp.verify.GenContractResponse;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendContractAdapterTask extends OnboardingSendAdapterTask<StoreContractRequest, StoreContractResponse, GenContractResponse> {

    @Override
    protected String getPartnerId(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) {
        return onboardingData.getPartnerId();
    }

    @Override
    protected String createAdapterRequest(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws Exception, BaseException, ValidatorException {
        StoreContractRequest request = onboardingData.getRequest();

        GenContractRequest adapterRequest = new GenContractRequest();
        adapterRequest.setRequestId(request.getRequestId());

        List<QueueContractResponse.AttachFileData> attachFileDatas = onboardingData.getTaskData(StoreContractQueueTask.NAME).getContent();
        Map<ContractType, FileContractLink> fileContractLinkMap = createContractFileDataMap(attachFileDatas, onboardingData);

        ApplicationData applicationData = request.getApplicationData();
        applicationData.setFileContractData(fileContractLinkMap);
        adapterRequest.setApplicationData(request.getApplicationData());
        String adapterMessage = Json.encode(adapterRequest);
        Log.MAIN.info("[Message send Adapter generate-contract] {}", adapterMessage);
        return adapterMessage;
    }

    public Map<ContractType, FileContractLink> createContractFileDataMap(List<QueueContractResponse.AttachFileData> attachFiles, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws Exception {
        ApplicationData applicationData = onboardingData.getRequest().getApplicationData();
        Map<ContractType, FileContractLink> fileContractLinkMap = new HashMap<>();
        OnboardingUtils.handleAttachFileData(attachFiles, ((attachFileData, contractType) -> {
            FileContractLink fileContractLink = createFileContractLink(applicationData, attachFileData, contractType);
            fileContractLinkMap.put(contractType, fileContractLink);
        }));
        Log.MAIN.info("Set contractFileData {} to TaskData", Json.encode(fileContractLinkMap));
        applicationData.setFileContractData(fileContractLinkMap);
        return fileContractLinkMap;
    }

    private FileContractLink createFileContractLink(ApplicationData applicationData, QueueContractResponse.AttachFileData attachFileData, ContractType contractType) {
        FileContractLink fileContractLink = new FileContractLink();
        fileContractLink.setApplicationId(applicationData.getApplicationId());
        fileContractLink.setPhoneNumber(applicationData.getPhoneNumber());
        fileContractLink.setPartnerId(applicationData.getPartnerId());
        fileContractLink.setName(attachFileData.getName());
        fileContractLink.setExpiredTime(attachFileData.getExpiredTime());
        fileContractLink.setLink(attachFileData.getLink());
        fileContractLink.setFileType(contractType.getType());
        fileContractLink.setPath(attachFileData.getPath());
        //set version = new for new onboarding-platform
        fileContractLink.setVersion(FileVersion.NEW);
        return fileContractLink;
    }

    @Override
    protected void processAdapterResponse(TaskData taskData, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData, GenContractResponse genContractResponse) throws Exception, BaseException, ValidatorException {
        Log.MAIN.info("Send adapter success");
        finish(onboardingData, taskData);
    }

    @Override
    protected boolean isAllowedSendAdapter(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        List<QueueContractResponse.AttachFileData> attachFileDatas = onboardingData.getTaskData(StoreContractQueueTask.NAME).getContent();
        if (Utils.isEmpty(attachFileDatas)) {
            Log.MAIN.info("Do not send Adapter with empty contract file");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.TASK_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }
}
