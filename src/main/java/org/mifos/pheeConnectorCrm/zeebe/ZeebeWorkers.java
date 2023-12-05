package org.mifos.pheeConnectorCrm.zeebe;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.json.JSONObject;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.utils.Headers;
import org.mifos.pheeConnectorCrm.utils.SpringWrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Component
public class ZeebeWorkers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;


    @PostConstruct
    public void setupWorkers() {
    //billerdetails
        zeebeClient.newWorker()
                .jobType("discover-bill")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    logWorkerDetails(job);
                    Map<String, Object> variables = job.getVariablesAsMap();
                    Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP,variables.get("payerFspId").toString())
                    .addHeader(BILL_ID,variables.get("billId").toString())
                            .build();
                    Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers,null);
                    producerTemplate.send("direct:biller-fetch", exchange);
                    Boolean response = exchange.getProperty(BILLER_FETCH_FAILED, Boolean.class);
                    variables.put(BILLER_FETCH_FAILED,response);
                    variables.put(BILLER_DETAILS, exchange.getProperty(BILLER_DETAILS, String.class));
                    variables.put(BILLER_ID, exchange.getProperty(BILLER_ID, String.class));
                    variables.put(BILLER_NAME, exchange.getProperty(BILLER_NAME, String.class));
                    variables.put(BILLER_TYPE, exchange.getProperty(BILLER_TYPE, String.class));
                    variables.put(BILLER_ACCOUNT, exchange.getProperty(BILLER_ACCOUNT, String.class));
                    logger.info("Zeebe variable {}", job.getVariablesAsMap());
                    zeebeClient.newCompleteCommand(job.getKey())
                            .variables(variables)
                            .send()
                            .join();;

        })
                .name("discover-bill")
                .maxJobsActive(workerMaxJobs)
                .open();

    //fetch bill details
        zeebeClient.newWorker().jobType("fetch-bill").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
                Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP,variables.get("payerFspId").toString())
                    .addHeader(BILL_ID,variables.get(BILL_ID).toString())
                    .build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers,null);
            exchange.setProperty(BILLER_ID, variables.get(BILLER_ID).toString());
            exchange.setProperty(BILLER_NAME, variables.get(BILLER_NAME).toString());
            producerTemplate.send("direct:bill-inquiry", exchange);
            variables.put(BILL_INQUIRY_RESPONSE, exchange.getProperty(BILL_INQUIRY_RESPONSE, String.class));
            variables.put(BILL_FETCH_FAILED,exchange.getProperty(BILL_FETCH_FAILED));
                zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
                logger.info("Zeebe variable {}", job.getVariablesAsMap());
        }).name("fetch-bill").maxJobsActive(workerMaxJobs).open();

//inquiry response to callback worker
        zeebeClient.newWorker().jobType("billFetchResponse").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader("Platform-TenantId", variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP,variables.get("payerFspId").toString())
                    .addHeader(BILL_ID,variables.get(BILL_ID).toString())
                    .build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers,null);
            exchange.setProperty(BILLER_DETAILS, variables.get(BILLER_DETAILS).toString());
            exchange.setProperty(CALLBACK_URL, variables.get(CALLBACK_URL).toString());
            exchange.setProperty(BILL_INQUIRY_RESPONSE, variables.get(BILL_INQUIRY_RESPONSE).toString());
            producerTemplate.send("direct:bill-inquiry-response", exchange);
            variables.put(BILL_PAY_RESPONSE, exchange.getIn().getBody(String.class));
            variables.put(BILL_PAY_FAILED,exchange.getProperty(BILL_PAY_FAILED));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.info("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billFetchResponse").maxJobsActive(workerMaxJobs).open();



//pay bill status
        zeebeClient.newWorker().jobType("billPay").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP,variables.get("payerFspId").toString()).build();
            BillPaymentsReqDTO requestBody = convertToBillPaymentsReqDTO(variables);
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers, objectMapper.writeValueAsString(requestBody));
            producerTemplate.send("direct:bill-payments", exchange);
            variables.put(BILL_PAY_RESPONSE, exchange.getIn().getBody(String.class));
            variables.put(BILL_PAY_FAILED,exchange.getProperty(BILL_PAY_FAILED));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.info("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billPay").maxJobsActive(workerMaxJobs).open();



        zeebeClient.newWorker().jobType("billRtpAck").handler((client, job) -> {
        logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
        logWorkerDetails(job);
        Map<String, Object> variables = job.getVariablesAsMap();
        Headers headers = new Headers.HeaderBuilder().addHeader("X-Platform-TenantId", variables.get(TENANT_ID).toString())
                .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString()).build();
        Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                headers,null);
        //check before implementing
        producerTemplate.send("direct:send-ack", exchange);
        zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
        logger.info("Zeebe variable {}", job.getVariablesAsMap());
    }).name("billRtpAck").maxJobsActive(workerMaxJobs).open();



 //billRTPResp
        zeebeClient.newWorker().jobType("billRTPResp").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader("X-Platform-TenantId", variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                   .build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers,null);
            producerTemplate.send("direct:bill-rtp-resp", exchange);
            variables.put("billRTPResponse", exchange.getIn().getBody(String.class));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.info("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billRTPResp").maxJobsActive(workerMaxJobs).open();


    //call payment notification
        zeebeClient.newWorker().jobType("notifyBill").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP,variables.get("payerFspId").toString()).build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(),
                    headers,null);
            exchange.setProperty(BILL_ID, variables.get(BILL_ID).toString());
            exchange.setProperty(CALLBACK_URL, variables.get(CALLBACK_URL).toString());
            producerTemplate.send("direct:start-paymentNotification", exchange);
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.info("Zeebe variable {}", job.getVariablesAsMap());
        }).name("notifyBill").maxJobsActive(workerMaxJobs).open();
}

    private BillPaymentsReqDTO convertToBillPaymentsReqDTO(Map<String, Object> variables) {
        BillPaymentsReqDTO billPaymentsReqDTO = new BillPaymentsReqDTO();
        return billPaymentsReqDTO;
    }

    private void logWorkerDetails(ActivatedJob job) {
        JSONObject jsonJob = new JSONObject();
        jsonJob.put("bpmnProcessId", job.getBpmnProcessId());
        jsonJob.put("elementInstanceKey", job.getElementInstanceKey());
        jsonJob.put("jobKey", job.getKey());
        jsonJob.put("jobType", job.getType());
        jsonJob.put("workflowElementId", job.getElementId());
        jsonJob.put("workflowDefinitionVersion", job.getProcessDefinitionVersion());
        jsonJob.put("workflowKey", job.getProcessDefinitionKey());
        jsonJob.put("workflowInstanceKey", job.getProcessInstanceKey());
        logger.info("Job started: {}", jsonJob.toString(4));
    }
}


