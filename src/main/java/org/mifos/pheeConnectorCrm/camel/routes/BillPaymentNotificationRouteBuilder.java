package org.mifos.pheeConnectorCrm.camel.routes;

import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.data.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.mifos.pheeConnectorCrm.utils.BillPayEnum.SUCCESS_RESPONSE_CODE;
import static org.mifos.pheeConnectorCrm.utils.BillPayEnum.SUCCESS_RESPONSE_MESSAGE;
import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Component
public class BillPaymentNotificationRouteBuilder extends ErrorHandlerRouteBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BillPaymentsReqDTO billPaymentsReqDTO;



    @Override
    public void configure() {

        from("direct:start-paymentNotification")
                .routeId("start-paymentNotification")
                .log("Bill Inquiry over, moving to bill payment")
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    JSONObject response = setResponseBody(exchange.getIn().getHeader(CLIENTCORRELATIONID).toString()
                            ,exchange.getProperty(BILL_ID).toString());
                    exchange.getIn().setHeader(PLATFORM_TENANT,exchange.getIn().getHeader(PLATFORM_TENANT));
                    exchange.getIn().setHeader(CLIENTCORRELATIONID,exchange.getIn().getHeader(CLIENTCORRELATIONID));
                    exchange.getIn().setHeader(PAYER_FSP,exchange.getIn().getHeader(PAYER_FSP));
                    exchange.getIn().setHeader(CALLBACK_URL,"https://webhook.site/2e4aa0de-4b8b-416f-a375-7085c0ec645e");
                    exchange.getIn().setBody(response.toString());
                })
                .log("Payment Notification Body: ${body}")
                .log("Payment Notification Headers: ${headers}")
                .toD("https://webhook.site/e8263cf3-47c9-4d20-9e2b-5866a75dbf65"+ "?bridgeEndpoint=true&throwExceptionOnFailure=false");

    }

    private JSONObject setResponseBody(String clientCorrelationId,String billId){

        JSONObject response = new JSONObject();
        billPaymentsReqDTO.setPaymentReferenceID(UUID.randomUUID().toString());
        billPaymentsReqDTO.setBillId(billId);
        billPaymentsReqDTO.setBillInquiryRequestId(UUID.randomUUID().toString());
        billPaymentsReqDTO.setClientCorrelationId(clientCorrelationId);
        response.put("BillPaymentsResponse", billPaymentsReqDTO);
        return response;
    }

}