package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.Address;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.Image;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public abstract class UpdateProfilePendingFormTask<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingTask<T, R> {

    public static final TaskName NAME = () -> "UPDATE_PROFILE_PENDING_FORM";

    public UpdateProfilePendingFormTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<T, R> onboardingData) throws BaseException, Exception, ValidatorException {
        ApplicationForm applicationForm = getApplicationForm(onboardingData);
        UserProfileInfo userProfileInfo = getUserProfile(onboardingData);
        updateProfileForm(onboardingData, applicationForm, userProfileInfo);
        finish(onboardingData, taskData);
    }

    protected void updateProfileForm(OnboardingData<T, R> onboardingData, ApplicationForm applicationForm, UserProfileInfo userProfileInfo) {
        ApplicationData applicationData = applicationForm.getApplicationData();
        applicationData.setFullName(userProfileInfo.getFullNameKyc());
        applicationData.setDob(userProfileInfo.getDobKyc());
        applicationData.setIdNumber(userProfileInfo.getPersonalIdKyc());
        if (Utils.isNotEmpty(userProfileInfo.getIdCardTypeKyc())) {
            applicationData.setIdType(userProfileInfo.getIdCardTypeKyc());
        }
        String email = applicationData.getEmail();
        if (Utils.isEmpty(email)) {
            applicationData.setEmail(userProfileInfo.getEmail());
        }
        if (Utils.isNotEmpty(userProfileInfo.getGenderKyc())) {
            applicationData.setGender(userProfileInfo.getGenderKyc());
        }
        applicationData.setIssueDate(userProfileInfo.getIssueDateKyc());
        applicationData.setIssuePlace(userProfileInfo.getIssuePlaceKyc());
        String permanentAddressKyc = userProfileInfo.getAddressKyc();
        if (Utils.isNotEmpty(permanentAddressKyc)) {
            Log.MAIN.info("Update new address kyc {} with agentId {} - applicationId {}", permanentAddressKyc, applicationData.getAgentId(), applicationData.getApplicationId());
            Address permanentAddress = new Address();
            permanentAddress.setFullAddress(permanentAddressKyc);
            applicationData.setPermanentAddress(permanentAddress);
        }
        OnboardingUtils.setImageApplicationFromUserProfile(applicationData, userProfileInfo);
        applicationData.setNationality(userProfileInfo.getNationalityKyc());
        applicationData.setExpiryDate(userProfileInfo.getExpiredDateKyc());
        applicationData.setOldIdNumber(userProfileInfo.getOldRecordsKyc());
        boolean isByPassValidatePath = getConfig().getByPassValidatePathImageByPartner().contains(onboardingData.getPartnerId());
        if ((isByPassValidatePath || Utils.isNotEmpty(userProfileInfo.getIdCardFaceKyc())) && Utils.isNotEmpty(userProfileInfo.getLinkIdCardFace())) {
            Image faceIdCardImage = new Image();
            faceIdCardImage.setPath(userProfileInfo.getIdCardFaceKyc());
            faceIdCardImage.setUrl(userProfileInfo.getLinkIdCardFace());
            applicationData.setFaceIdCardImage(faceIdCardImage);
        }
        applicationData.setPreviousIdNumber(userProfileInfo.getKycPreviousNumber());
        applicationData.setC06TimeVerify(userProfileInfo.getC06TimeVerify());
        applicationData.setKycC06Partner(userProfileInfo.getKycC06Partner());
    }

    protected abstract ApplicationForm getApplicationForm(OnboardingData<T, R> onboardingData);

    protected abstract UserProfileInfo getUserProfile(OnboardingData<T, R> onboardingData);

}
