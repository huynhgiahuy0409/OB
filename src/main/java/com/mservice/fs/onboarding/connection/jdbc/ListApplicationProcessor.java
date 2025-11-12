package com.mservice.fs.onboarding.connection.jdbc;

import com.mservice.fs.jdbc.mapping.JdbcTransformer;
import com.mservice.fs.jdbc.processor.CallableInputParam;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.model.ApplicationDataLite;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.PeriodUnit;
import com.mservice.fs.onboarding.model.RoutingPackageStatus;
import com.mservice.fs.onboarding.model.application.ApplicationCheckStatusDB;
import com.mservice.fs.onboarding.model.application.ApplicationCheckStatusDbWrapper;
import com.mservice.fs.onboarding.model.application.init.ApplicationDataInit;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hoang.thai
 * on 11/6/2023
 */
public class ListApplicationProcessor extends CallableProcessor<ApplicationCheckStatusDbWrapper> {

    @Override
    protected ApplicationCheckStatusDbWrapper processWithStatement(CallableStatement cs) throws Exception, BaseException {
        ResultSet rsApplicationValidDeDup = (ResultSet) cs.getObject("p_application_by_agent");
        List<ApplicationDataInit> applicationDbValidDeDups = JdbcTransformer.toObjects(rsApplicationValidDeDup, ApplicationDataInit.class);

        ResultSet rsApplication = (ResultSet) cs.getObject("P_RESULT");
        List<ApplicationDataLite> applicationDataLites = new ArrayList<>();
        JdbcTransformer.toObjects(rsApplication, ApplicationCheckStatusDB.class,
                data -> {
                    PackageInfo packageInfo = getPackageInfo(data);
                    String availableLoanAmountsStr = rsApplication.getString("AVAILABLE_AMOUNTS");
                    if (Utils.isNotEmpty(availableLoanAmountsStr)){
                        List<Long> availableLoanAmounts = Arrays.stream(availableLoanAmountsStr.split(CommonConstant.COMMA_SPLITTER))
                                .map(String::trim)
                                .map(Long::valueOf)
                                .collect(Collectors.toList());
                        packageInfo.setAvailableLoanAmounts(availableLoanAmounts);
                    }

                    ApplicationDataLite applicationDataLite = new ApplicationDataLite();
                    applicationDataLite.setApplicationId(data.getContractId());
                    Date createdDate = data.getCreateTime();
                    if (Utils.isNotEmpty(createdDate)) {
                        applicationDataLite.setCreatedDate(data.getCreateTime().toInstant().toEpochMilli());
                    }
                    Date modifiedDate = data.getLastModified();
                    if (Utils.isNotEmpty(modifiedDate)) {
                        applicationDataLite.setModifiedDateInMillis(data.getLastModified().toInstant().toEpochMilli());
                    }
                    applicationDataLite.setModifiedName(data.getModifiedName());
                    try {
                        String state = data.getState();
                        if (Utils.isNotEmpty(state)) {
                            applicationDataLite.setState(ApplicationState.valueOf(state));
                        }
                        String newStatus = data.getNewStatus();
                        if (Utils.isNotEmpty(newStatus)) {
                            applicationDataLite.setStatus(ApplicationStatus.valueOf(newStatus));
                        }
                        String routingPackageStatus = data.getRoutingPackageStatus();
                        applicationDataLite.setRoutingPackageStatus(Utils.isNotEmpty(routingPackageStatus) ? RoutingPackageStatus.valueOf(routingPackageStatus) : RoutingPackageStatus.UNKNOWN);
                    }
                    catch (Exception e){
                        Log.MAIN.info("Can't valueOf enum with data {}: ", Json.encode(data), e);
                        return;
                    }

                    if (Utils.isNotEmpty(packageInfo.getPackageCode())) {
                        applicationDataLite.setChosenPackage(packageInfo);
                    }
                    applicationDataLite.setFullName(data.getFullName());
                    applicationDataLite.setReasonId(data.getReasonId());
                    applicationDataLite.setReasonMessage(data.getReasonMessage());
                    applicationDataLite.setServiceId(data.getServiceCode());
                    applicationDataLite.setPartnerId(data.getPartnerCode());
                    applicationDataLites.add(applicationDataLite);
                }
        );

        ApplicationCheckStatusDbWrapper dbWrapper = new ApplicationCheckStatusDbWrapper();
        dbWrapper.setApplicationDataLites(applicationDataLites);
        dbWrapper.setApplicationDbValidDeDups(applicationDbValidDeDups);

        return dbWrapper;
    }

    private PackageInfo getPackageInfo(ApplicationCheckStatusDB data) throws IOException {
        if (Utils.isNotEmpty(data.getFullPackageRaw())) {
            Log.MAIN.info("FullPackageRaw is not empty, contract is old onboarding");
            return JsonUtil.fromString(data.getFullPackageRaw(), PackageInfo.class);
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setProductGroup(data.getProductGroup());
        packageInfo.setTenor(data.getTenor());
        packageInfo.setLoanAmount(data.getLoanAmount());
        packageInfo.setDisbursedAmount(data.getDisbursedAmount());
        packageInfo.setInterestAmount(data.getInterestAmount());
        String interestUnit = data.getInterestUnit();
        if (Utils.isNotEmpty(interestUnit)) {
            packageInfo.setInterestUnit(PeriodUnit.valueOf(interestUnit));
        }
        packageInfo.setServiceFee(data.getServiceFee());
        packageInfo.setCollectionFee(data.getCollectionFee());
        packageInfo.setDisbursedFee(data.getDisbursedFee());
        packageInfo.setLateInterest(data.getLateInterest());
        packageInfo.setLateFee(data.getLateFee());
        packageInfo.setInterest(data.getInterest());
        packageInfo.setPaymentAmount(data.getPaymentAmount());
        packageInfo.setEmi(data.getEmi());
        String tenorUnit = data.getTenorUnit();
        if (Utils.isNotEmpty(tenorUnit)) {
            packageInfo.setTenorUnit(PeriodUnit.valueOf(tenorUnit));
        }
        packageInfo.setSegmentUser(data.getSegmentUser());
        packageInfo.setLenderLogic(data.getLenderLogic());
        packageInfo.setDueDay(data.getDueDay());
        packageInfo.setPackageGroup(data.getPackageGroup());
        packageInfo.setPackageName(data.getPackageName());
        packageInfo.setPackageCode(data.getPackageCode());
        packageInfo.setLenderId(data.getLenderId());
        packageInfo.setRank(data.getRank());
        packageInfo.setMinLoanAmount(data.getMinLoanAmount());
        packageInfo.setMaxLoanAmount(data.getMaxLoanAmount());
        return packageInfo;
    }

    public ApplicationCheckStatusDbWrapper execute(String serviceId, String agentId, String personalId, String serviceGroup) throws BaseException {
        try {
            return run(
                    new CallableInputParam("p_agent_id", agentId),
                    new CallableInputParam("p_service_id", serviceId),
                    new CallableInputParam("p_personal_id", personalId),
                    new CallableInputParam("p_service_group", serviceGroup)
            );
        } catch (Exception ex) {
            Log.MAIN.error("[GetListApplicationProcessor] Error when execute get data from database", ex);
            throw new BaseException(CommonErrorCode.DB_ERROR);
        }
    }
}
