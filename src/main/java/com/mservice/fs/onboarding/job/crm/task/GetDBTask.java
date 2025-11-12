package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.connection.jdbc.GetListApplicationCrm;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.crm.*;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import java.util.ArrayList;
import java.util.List;

public class GetDBTask<T extends CrmRequest, R extends CrmResponse> extends Task<PlatformData<T,R>,T,R, OnboardingConfig> {

    public static final TaskName NAME = () -> "GET_DB_TASK";

    @Autowire
    private GetListApplicationCrm getListApplicationCrm;

    public GetDBTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, PlatformData<T, R> platformData) throws BaseException, Exception, ValidatorException {
        List<LoanInfo> loanInfos = new ArrayList<>();

        CrmConfig.Config config = platformData.getTaskData(LoadCrmConfigTask.NAME).getContent();
        UserProfileInfo userProfileInfo = platformData.getTaskData(CrmGetUserProfileTask.NAME).getContent();
        String agentId = userProfileInfo.getAgent();

        CrmRequest request = platformData.getRequest();

        List<ApplicationForm> applicationForms = getListApplicationCrm.execute(agentId, request.getBeginDate(), request.getEndDate());

        applicationForms.forEach(form -> {
            LoanInfo loanInfo = new LoanInfo();
            ApplicationData data = form.getApplicationData();
            CrmConfig.Status status = config.getApplicationStatusMap().get(data.getStatus().name());

            loanInfo.setCreateTime(data.getCreatedDate());
            loanInfo.setPartnerId(data.getPartnerId());
            loanInfo.setLastModified(data.getModifiedDateInMillis());
            loanInfo.setPhoneNumber(data.getPhoneNumber());
            loanInfo.setFullName(data.getFullName());
            loanInfo.setServiceId(data.getServiceId());
            loanInfo.setPersonalId(data.getIdNumber());
            loanInfo.setStatus(data.getStatus());
            loanInfo.setLoanId(data.getApplicationId());
            loanInfo.setType(CrmType.DATABASE);
            loanInfo.setStatusMessage(status.getDescription());
            loanInfos.add(loanInfo);
        });

        taskData.setContent(loanInfos);
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
