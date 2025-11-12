package com.mservice.fs.onboarding.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.ByteString;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.Config;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.client.Handler;
import com.mservice.fs.grpc.model.MessageRpl;
import com.mservice.fs.grpc.model.StandardMessage;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.ApplicationDataInfo;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.FSRuleName;
import com.mservice.fs.model.PlatformResponse;
import com.mservice.fs.model.Request;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.JobData;
import com.mservice.fs.rule.DataCondition;
import com.mservice.fs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public abstract class SendingPlatformListener<D extends JobData<T, R>, T extends Request, R extends PlatformResponse, C extends Config> extends AbstractListener<D, T, R, C> {

    @Autowire(name = ApplicationDataInfo.DATA_SERVICE_NAME)
    private DataService<ApplicationDataInfo> applicationDataInfo;

    public SendingPlatformListener(String name) {
        super(name);
    }

    @Override
    public void execute(D baseData) throws Throwable {
        byte[] request = new byte[]{};
        try {
            request = createCrossPlatformRequest(baseData);
            Map<String, String> headers = convertHeaders(baseData.getBase().getHeaders());
            StandardMessage protoMessage = StandardMessage.newBuilder()
                    .setData(ByteString.copyFrom(request))
                    .setServiceId(baseData.getServiceId())
                    .setTraceId(baseData.getTraceId())
                    .setType(getProcessName(baseData))
                    .setCoreWalletId(baseData.getInitiator())
                    .setAgentId(baseData.getInitiatorId())
                    .putAllHeaders(headers)
                    .build();
            ProxyGrpcClient proxyGrpcClient = getProxyGrpcClient(baseData);

            proxyGrpcClient.send(baseData.getBase(), protoMessage, getMetadata(baseData), new Handler<>() {
                @Override
                protected void error(Throwable t) {
                    baseData.initLog(getName());
                    Log.MAIN.error("Error when call adapter [{}] - service [{}]: ", proxyGrpcClient, baseData.getServiceId(), t);
                }

                @Override
                protected void handle(MessageRpl messageRpl) {
                    workers.executeJob(getName(), () -> {
                        try {
                            baseData.initLog(getName());
                            Log.MAIN.info("Got Response ResultCode:[{}], ResultMessage: [{}]", messageRpl.getResultCode(), messageRpl.getResultMessage());
                            createCrossPlatformResponse(messageRpl.getPayload());
                        } catch (Exception ex) {
                            Log.MAIN.error("CRITICAL|| Error when execute handle - [{}] - service [{}] :", proxyGrpcClient, baseData.getServiceId(), ex);
                        }
                    });
                }

                @Override
                protected void timeout() {
                    baseData.initLog(getName());
                    Log.MAIN.info("Timeout when send Message to platform - [{}] - service [{}]", proxyGrpcClient, baseData.getServiceId());
                }

            });
        } catch (Exception ex) {
            Log.MAIN.error("CRITICAL|| Error when send message [{}]: ", new String(request), ex);
        } catch (BaseException ex) {
            Log.MAIN.error("error when exec [{}]:", new String(request), ex);
        } catch (ValidatorException ex) {
            Log.MAIN.error("Validate Error when send to platform :", ex);
        }
    }

    protected String getProcessName(D baseData) {
        return baseData.getProcessName();
    }

    private Map<String, String> convertHeaders(Map<String, Object> headers) {
        Map<String, String> convert = new HashMap<>();
        if (Utils.isNotEmpty(headers)) {
            for (var entry : headers.entrySet()) {
                convert.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return convert;
    }

    private Map<String, Object> getMetadata(D jobData) throws Exception, BaseException, ValidatorException {
        Map<String, Object> metadata = new HashMap<>();
        DataCondition dataCondition = applicationDataInfo.getData().getRule(FSRuleName.GRPC_METADATA, jobData.getServiceId());
        if (Utils.isNotEmpty(dataCondition)) {
            Log.MAIN.info("Apply Data Condition:{}", dataCondition.getId());
            String data = dataCondition.process(jobData);
            if (Utils.isNotEmpty(data)) {
                metadata.putAll(Json.decodeValue(data, new TypeReference<>() {
                }));
            }
            Log.MAIN.info("Metadata: {}", metadata);
        }
        return metadata;
    }


    protected abstract ProxyGrpcClient getProxyGrpcClient(D platformData);

    protected abstract byte[] createCrossPlatformRequest(D platformData) throws Exception;

    protected abstract void createCrossPlatformResponse(String payload) throws Exception;

}
