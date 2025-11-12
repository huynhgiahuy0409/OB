package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.crm.CrmRequest;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.onboarding.model.crm.LoanInfo;
import com.mservice.fs.onboarding.model.crm.LoanListWrapper;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;

import java.util.ArrayList;
import java.util.List;

public class BuildResponseTask<T extends CrmRequest> extends Task<PlatformData<T,CrmResponse>,T,CrmResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "BUILD_RESPONSE";


    public BuildResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<T, CrmResponse> platformData) throws BaseException, Exception, ValidatorException {


        LoanListWrapper loanListWrapper = platformData.getTaskData(GetCacheTask.NAME).getContent();
        List<LoanInfo> cacheLoanInfos = loanListWrapper.getFetchList();
        List<LoanInfo> dbLoanInfos = platformData.getTaskData(GetDBTask.NAME).getContent();

        List<LoanInfo> loanInfos = new ArrayList<>();
        loanInfos.addAll(cacheLoanInfos);
        loanInfos.addAll(dbLoanInfos);

        CrmResponse response = new CrmResponse();
        response.setResultCode(CommonErrorCode.SUCCESS);
        response.setLoanInfo(loanInfos);
        platformData.setResponse(response);
        finish(platformData, taskData);
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
