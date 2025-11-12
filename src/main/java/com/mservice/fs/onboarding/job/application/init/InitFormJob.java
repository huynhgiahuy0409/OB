package com.mservice.fs.onboarding.job.application.init;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.*;
import com.mservice.fs.onboarding.job.application.init.listener.InitFormTrackingRequestListener;
import com.mservice.fs.onboarding.job.application.init.listener.InitStoreRejectListener;
import com.mservice.fs.onboarding.job.application.init.listener.PartnerResultAIListener;
import com.mservice.fs.onboarding.job.application.init.listener.UpdateApplicationFormListener;
import com.mservice.fs.onboarding.job.application.init.task.*;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.task.LoadDataTask;
import com.mservice.fs.utils.Utils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author hoang.thai
 * on 11/7/2023
 */
@Processor(name = { OnboardingProcessor.INIT_APPLICATION_FORM, OnboardingProcessor.INIT_APPLICATION_FORM_QUICK})
public class InitFormJob extends OnboardingJob<InitFormRequest, InitFormResponse> {

    public InitFormJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<InitFormRequest, InitFormResponse>, InitFormRequest, InitFormResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new ValidatePackageTask(),
                new AbsGetUserProfileTask<>(),
                new InitFormGetDBTask(),
                new InitFormCheckDeDupMomoTask(),
                new CacheTask(),
                new HandlePendingFormTask(),
                new InitUpdateProfileTask(),
                new InitFormVerifyUserProfileTask(),
                new InitFormCheckKnockOutRuleTask(),
                new InitFormPackageAi(),
                new InitFormLendingPackageTask(),
                new InitDeDupTask(),
                new InitFormModifiedResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<InitFormRequest, InitFormResponse>, InitFormRequest, InitFormResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateApplicationFormListener(),
                new InitFormTrackingRequestListener(),
                new InitStoreRejectListener(),
                new PartnerResultAIListener(),
                new MidKorRejectScreenLogListener<>()
        );
    }

    @Override
    protected LinkedHashMap<TaskName, TaskName> getTaskOrders() {
        LinkedHashMap<TaskName, TaskName> taskOrders = new LinkedHashMap<>();
        taskOrders.put(InitFormLendingPackageTask.NAME, InitFormModifiedResponseTask.NAME);
        taskOrders.put(InitDeDupTask.NAME, InitFormModifiedResponseTask.NAME);
        return taskOrders;
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<InitFormRequest, InitFormResponse> jobData, InitFormResponse response) {
        TaskData taskData = jobData.getTaskData(HandlePendingFormTask.NAME);
        if (Utils.isNotEmpty(taskData) && Utils.isNotEmpty(taskData.getContent())) {
            Log.MAIN.info("Add Data applicationForm to response");
            ApplicationForm applicationForm = taskData.getContent();
            response.setApplicationData(applicationForm.getApplicationData());
        }
        super.addDataBeforeReply(jobData, response);
    }

    @Override
    protected boolean isReplaceRenderData(OnboardingData<InitFormRequest, InitFormResponse> data) {
        return Utils.isNotEmpty(data.getRequest()) && Boolean.TRUE.equals(data.getRequest().getIsNfcAvailable());
    }
}
