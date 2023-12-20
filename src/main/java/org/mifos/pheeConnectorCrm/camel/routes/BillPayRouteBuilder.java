package org.mifos.pheeConnectorCrm.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.pheeConnectorCrm.data.Bill;
import org.mifos.pheeConnectorCrm.data.BillInquiryResponseDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsResponseDTO;
import org.mifos.pheeConnectorCrm.utils.BillPayEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.mifos.pheeConnectorCrm.utils.BillPayEnum.*;
import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Component
public class BillPayRouteBuilder extends ErrorHandlerRouteBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BillPaymentsResponseDTO billPaymentsResponseDTO;



    @Override
    public void configure() {

        from("direct:bill-payments")
                .routeId("bill-payments")
                .log("Received request for bill payments")
                .unmarshal()
                .json(JsonLibrary.Jackson, BillPaymentsReqDTO.class)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .process(exchange -> {
                    logger.info("Bill Payments Request: " + exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    BillPaymentsResponseDTO response = setResponseBody(exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    exchange.setProperty("billPayFailed", false);
                    exchange.setProperty(BILL_PAY_RESPONSE,response);
                    exchange.setProperty("reason",billPaymentsResponseDTO.getReason());
                    exchange.setProperty("code",billPaymentsResponseDTO.getCode());
                    exchange.setProperty("status",billPaymentsResponseDTO.getStatus());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(response);
                    exchange.getIn().setBody(jsonString);
                    logger.info("Bill Payments Response: " + response);
                });

    }

    private BillPaymentsResponseDTO setResponseBody(BillPaymentsReqDTO billPaymentsReqDTO) {

        billPaymentsResponseDTO.setBillId(billPaymentsReqDTO.getBillId());
        billPaymentsResponseDTO.setCode(SUCCESS_RESPONSE_CODE.getValue());
        billPaymentsResponseDTO.setReason(SUCCESS_RESPONSE_MESSAGE.getValue());
        billPaymentsResponseDTO.setStatus(SUCCESS_STATUS.getValue());
        billPaymentsResponseDTO.setRequestID(billPaymentsReqDTO.getBillInquiryRequestId());
        billPaymentsResponseDTO.setPaymentReferenceID(billPaymentsReqDTO.getPaymentReferenceID());
        return billPaymentsResponseDTO;
    }

}