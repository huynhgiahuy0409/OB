package com.mservice.fs.onboarding.job.contract.store.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.AttachFileConfig;
import com.mservice.fs.onboarding.config.ContractConfig;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.onboarding.model.verifyotp.AttachFile;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractRequest;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractResponse;
import com.mservice.fs.processor.JobData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.rabbit.sending.QueueTask;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Map;

public class StoreContractQueueTask extends QueueTask<OnboardingData<StoreContractRequest, StoreContractResponse>, StoreContractRequest, StoreContractResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "STORE_CONTRACT_QUEUE";

    @Autowire(name = "GetContract")
    private RabbitSendingService storeContractFileService;

    public StoreContractQueueTask() {
        super(NAME);
    }

    @Override
    protected void processWithResponse(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData, TaskData taskData, byte[] bytes) throws ValidatorException, BaseException, Exception {
        QueueContractResponse queueContractResponse = JsonUtil.fromByteArray(bytes, QueueContractResponse.class);
        Log.MAIN.info("Receive response from queue: {}", Json.encode(queueContractResponse));
        Integer resultCode = queueContractResponse.getResultCode();
        List<QueueContractResponse.AttachFileData> attachFiles = queueContractResponse.getAttachFiles();
        if (CommonErrorCode.SUCCESS.getCode().equals(resultCode) && Utils.isNotEmpty(attachFiles)) {
            Log.MAIN.info("Store link contract success with resultCode {} or attachFiles {}", resultCode, attachFiles);
            taskData.setContent(attachFiles);
        } else {
            Log.MAIN.fatal("Gen link contract fail with resultCode {} or attachFiles is empty {}", resultCode, attachFiles);
            throw new BaseException(OnboardingErrorCode.STORE_CONTRACT_QUEUE_FAIL);
        }
        finish(onboardingData, taskData);
    }

    @Override
    protected void processWithLateResponse(OnboardingData<StoreContractRequest, StoreContractResponse> storeContractRequestStoreContractResponseOnboardingData, TaskData taskData, byte[] bytes) throws ValidatorException, BaseException, Exception {
        Log.MAIN.info("[StoreContractQueueTask] Received late response: [{}]", new String(bytes));
    }

    @Override
    protected void processTimeout(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData, TaskData taskData) throws ValidatorException, BaseException, Exception {
        Log.MAIN.error("[StoreContractQueueTask] Timeout when execute call store queue contract do finished !!!");
        this.timeout(onboardingData, taskData);
    }

    @Override
    protected void error(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData, TaskData taskData, Throwable throwable) throws ValidatorException, BaseException, Exception {
        Log.MAIN.error("[StoreContractQueueTask] Error when execute call store queue contract do finished:", throwable);
        this.error(onboardingData, taskData);
    }

    @Override
    protected String createQueueRequest(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData, StoreContractRequest storeContractRequest) throws ValidatorException, BaseException, Exception {
        List<AttachFile> attachFiles = onboardingData.getTaskData(GenerateContractTask.NAME).getContent();
        QueueContractRequest contractRequest = new QueueContractRequest();
        contractRequest.setRequestId(onboardingData.getRequest().getRequestId());
        contractRequest.setAttachFiles(attachFiles);
        contractRequest.setPartnerId(onboardingData.getPartnerId());
        contractRequest.setModuleName(getResource().getModuleName());
        contractRequest.setWalletId(onboardingData.getInitiator());
        return contractRequest.toString();
    }

    @Override
    protected Map<String, Object> createRequestHeaders(JobData<StoreContractRequest, StoreContractResponse> jobData) {
        Map<String, Object> headers = super.createRequestHeaders(jobData);
        ContractConfig contractConfig = getConfig().getContractConfig();
        AttachFileConfig attachFileStoreConfig = contractConfig.getStoreFilePDF();
        headers.put(Base.PROCESS_NAME_FIELD, attachFileStoreConfig.getProcessName());
        headers.put(Base.SERVICE_ID_FIELD_NAME, jobData.getServiceId());
        headers.put(Base.PARTNER_ID_FIELD_NAME, jobData.getPartnerId());
        return headers;
    }

    @Override
    protected RabbitSendingService getRabbitSendingService() {
        return storeContractFileService;
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
