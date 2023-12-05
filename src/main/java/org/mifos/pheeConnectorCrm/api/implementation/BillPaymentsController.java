package org.mifos.pheeConnectorCrm.api.implementation;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.mifos.pheeConnectorCrm.api.definition.BillPaymentsApi;
import org.mifos.pheeConnectorCrm.data.BillInquiryResponseDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsResponseDTO;
import org.mifos.pheeConnectorCrm.service.BillPaymentsService;
import org.mifos.pheeConnectorCrm.utils.Headers;
import org.mifos.pheeConnectorCrm.utils.SpringWrapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@RestController
public class BillPaymentsController implements BillPaymentsApi {
    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public BillPaymentsResponseDTO billPayments(String tenantId, String correlationId,
                                                               String payerFspId, BillPaymentsReqDTO requestBody)
            throws ExecutionException, InterruptedException, JsonProcessingException {
        Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, tenantId)
                .addHeader(CLIENTCORRELATIONID, correlationId)
                .addHeader(PAYER_FSP, payerFspId)
                .build();
        Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers,
                objectMapper.writeValueAsString(requestBody));
        producerTemplate.send("direct:bill-payments", exchange);
        return exchange.getIn().getBody(BillPaymentsResponseDTO.class);
    }
}
