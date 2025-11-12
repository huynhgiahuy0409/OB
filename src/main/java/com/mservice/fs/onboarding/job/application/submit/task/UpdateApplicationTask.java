package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.LoanGoal;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hoang.thai
 * on 12/29/2023
 */
public class UpdateApplicationTask extends ApplicationTask<SubmitRequest, SubmitResponse> {

    private static final String LOAN_GOAL = "loanGoal";

    @Override
    protected void updateApplicationForm(ApplicationForm applicationForm, SubmitRequest request, OnboardingData<SubmitRequest, SubmitResponse> jobData, UserProfileInfo userProfileInfo, ServiceObInfo serviceObInfo) throws Exception {
        Log.MAIN.info("Start update redirectTo with applicationId {} - phoneNumber {} - redirect to: {}", applicationForm.getApplicationData().getApplicationId(), applicationForm.getApplicationData().getInitiator(), jobData.getProcessName());
        applicationForm.setRedirectTo(jobData.getProcessName());
        OnboardingUtils.mapApplicationInfoToApplicationData(request.getApplicationInfo(), applicationForm.getApplicationData());
        updateApplicationStatus(applicationForm, jobData, userProfileInfo);
        updateDefaultApplicationData(applicationForm, serviceObInfo);
    }

    private void updateDefaultApplicationData(ApplicationForm applicationForm, ServiceObInfo serviceObInfo) {
        ApplicationData applicationData = applicationForm.getApplicationData();
        Map<String, Object> applicationAdditionalData = applicationData.getApplicationAdditionalData();
        String loanGoalDefault = serviceObInfo.getLoanGoalDefault();
        if (Utils.isEmpty(applicationAdditionalData)) {
            Log.MAIN.info("ApplicationAdditionalData is empty create new");
            applicationAdditionalData = new HashMap<>();
        }
        if (Utils.isEmpty(applicationAdditionalData.get(LOAN_GOAL)) && Utils.isNotEmpty(loanGoalDefault)) {
            try {
                LoanGoal loanGoal = JsonUtil.fromString(loanGoalDefault, LoanGoal.class);
                Log.MAIN.info("Update default loanGoal {} - applicationAdditionalData {}", loanGoalDefault, applicationAdditionalData);
                applicationAdditionalData.put(LOAN_GOAL, loanGoal);
                applicationData.setApplicationAdditionalData(applicationAdditionalData);
            } catch (Exception e) {
                Log.MAIN.error("Error when update default loanGoalDefault {}", loanGoalDefault, e);
            }
        }
    }
}
