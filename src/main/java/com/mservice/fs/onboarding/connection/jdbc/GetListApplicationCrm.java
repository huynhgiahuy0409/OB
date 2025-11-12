package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author phat.duong1
 * on 25/11/2024
 */
public class GetListApplicationCrm extends CallableProcessor<List<ApplicationForm>> {

    @Override
    protected List<ApplicationForm> processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ResultSet rs = (ResultSet) cs.getObject("P_RESULT");
        List<ApplicationForm> applicationForms = new ArrayList<>();
        while (rs.next()) {
            ApplicationForm applicationForm = new ApplicationForm();
            ApplicationData applicationData = new ApplicationData();
            try {
                applicationData.setStatus(ApplicationStatus.valueOf(rs.getString("NEW_STATUS")));
            } catch (Exception e) {
                continue;
            }
            applicationData.setAgentId(rs.getString("AGENT_ID"));
            applicationData.setPartnerId(rs.getString("PARTNER_CODE"));
            applicationData.setCreatedDate(rs.getTimestamp("CREATE_TIME").getTime());
            applicationData.setModifiedDateInMillis(rs.getTimestamp("LAST_MODIFIED").getTime());
            applicationData.setPhoneNumber(rs.getString("PHONE_NUMBER"));
            applicationData.setFullName(rs.getString("FULL_NAME"));
            applicationData.setServiceId(rs.getString("SERVICE_CODE"));
            applicationData.setIdNumber(rs.getString("PERSONAL_ID"));
            applicationData.setApplicationId(rs.getString("LOAN_ID"));
            applicationForm.setApplicationData(applicationData);
            applicationForms.add(applicationForm);
        }
        return applicationForms;
    }

    public List<ApplicationForm> execute(String agentId, String beginDate, String endDate) throws BaseException {
        try {
            return run(
                    new CallableInputParam("p_agent_id", agentId),
                    new CallableInputParam("p_begin_date", beginDate),
                    new CallableInputParam("p_end_date", endDate)
            );
        } catch (Exception ex) {
            Log.MAIN.info("[GetApplicationFormByServiceAgent] Error when execute get data from database", ex);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }
    }
}
