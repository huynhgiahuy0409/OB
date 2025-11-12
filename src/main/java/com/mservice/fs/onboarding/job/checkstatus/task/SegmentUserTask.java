package com.mservice.fs.onboarding.job.checkstatus.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.RoutingPackageStatus;
import com.mservice.fs.onboarding.model.UserType;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.model.api.status.WaitRoutingForm;
import com.mservice.fs.onboarding.model.application.ApplicationCheckStatusDbWrapper;
import com.mservice.fs.onboarding.model.status.CheckStatusData;
import com.mservice.fs.onboarding.model.status.SegmentData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author hoang.thai
 * on 11/30/2023
 */
public class SegmentUserTask extends OnboardingTask<OnboardingStatusRequest, OnboardingStatusResponse> {

    public static final TaskName NAME = () -> "SEGMENT_USER";

    public SegmentUserTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> jobData) throws BaseException, Exception, ValidatorException {
        ApplicationCheckStatusDbWrapper checkStatusDbWrapper = jobData.getTaskData(GetDataDeDupDBTask.NAME).getContent();
        CheckStatusData applicationCache = jobData.getTaskData(CacheTask.NAME).getContent();
        List<ApplicationForm> applicationForms = applicationCache.getApplicationForms();
        SegmentData segmentData = createSegmentData(checkStatusDbWrapper, applicationForms);
        Log.MAIN.info("Segment User: {}", segmentData);
        taskData.setContent(segmentData);
        finish(jobData, taskData);
    }

    private SegmentData createSegmentData(ApplicationCheckStatusDbWrapper checkStatusDbWrapper, List<ApplicationForm> pendingFormCaches) throws BaseException {
        SegmentData segmentData = new SegmentData();

        List<ApplicationDataLite> activeApplications = new ArrayList<>();
        List<ApplicationDataLite> liquidationApplications = new ArrayList<>();
        List<ApplicationDataLite> closedApplications = new ArrayList<>();
        List<WaitRoutingForm> waitRoutingForms = new ArrayList<>();

        List<ApplicationDataLite> pendingDbApplications = new ArrayList<>();

        for (ApplicationDataLite applicationDataLite : checkStatusDbWrapper.getApplicationDataLites()) {
            switch (applicationDataLite.getStatus().getState()) {
                case ACTIVE:
                    activeApplications.add(applicationDataLite);
                    break;
                case LIQUIDATION:
                    liquidationApplications.add(applicationDataLite);
                    break;
                case CLOSE:
                case CANCEL:
                    closedApplications.add(applicationDataLite);
                    if (RoutingPackageStatus.WAITING_AI.equals(applicationDataLite.getRoutingPackageStatus())) {
                        WaitRoutingForm waitRoutingForm = new WaitRoutingForm();
                        waitRoutingForm.setApplicationId(applicationDataLite.getApplicationId());
                        waitRoutingForm.setModifiedDateInMillis(applicationDataLite.getModifiedDateInMillis());
                        waitRoutingForms.add(waitRoutingForm);
                    }
                    break;
                case PENDING:
                case LOCK:
                    pendingDbApplications.add(applicationDataLite);
                    break;
                default:
                    break;
            }
        }

        List<ApplicationDataLite> submittedForms = new ArrayList<>();
        submittedForms.addAll(activeApplications);
        submittedForms.addAll(liquidationApplications);
        submittedForms.addAll(closedApplications);

        List<ApplicationDataLite> activeForms = new ArrayList<>(activeApplications);

        List<ApplicationDataLite> closedForms = new ArrayList<>();
        closedForms.addAll(liquidationApplications);
        closedForms.addAll(closedApplications);

        //Sort data by modifiedDate
        submittedForms.sort(Comparator.comparingLong(ApplicationDataLite::getModifiedDateInMillis).reversed());
        activeForms.sort(Comparator.comparingLong(ApplicationDataLite::getModifiedDateInMillis).reversed());
        closedForms.sort(Comparator.comparingLong(ApplicationDataLite::getModifiedDateInMillis).reversed());

        segmentData.setSubmittedForms(submittedForms);
        segmentData.setActiveFormsForms(activeForms);
        segmentData.setClosedForms(closedForms);
        segmentData.setUserType(getUserType(activeApplications, liquidationApplications, closedApplications, pendingFormCaches));
        segmentData.setWaitRoutingForms(waitRoutingForms);
        segmentData.setPendingStateForms(pendingDbApplications);
        return segmentData;
    }

    public UserType getUserType(List<ApplicationDataLite> activeApplications, List<ApplicationDataLite> liquidationApplications, List<ApplicationDataLite> closedApplications, List<ApplicationForm> pendingFormCaches) {
        if (Utils.isEmpty(activeApplications) &&
                Utils.isEmpty(liquidationApplications) &&
                Utils.isEmpty(closedApplications) &&
                Utils.isEmpty(pendingFormCaches)) {
            return UserType.NEW;
        }
        if (Utils.isNotEmpty(closedApplications) &&
                Utils.isEmpty(activeApplications) &&
                Utils.isEmpty(liquidationApplications) &&
                Utils.isEmpty(pendingFormCaches)) {
            return UserType.REJECTED_USER;
        }
        return UserType.OLD;
    }

}
