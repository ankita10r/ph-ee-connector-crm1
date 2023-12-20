package org.mifos.pheeConnectorCrm.camel.routes;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.pheeConnectorCrm.data.Bill;
import org.mifos.pheeConnectorCrm.data.BillInquiryResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.mifos.pheeConnectorCrm.utils.BillPayEnum.SUCCESS_RESPONSE_CODE;
import static org.mifos.pheeConnectorCrm.utils.BillPayEnum.SUCCESS_RESPONSE_MESSAGE;
import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Component
public class BillInquiryRouteBuilder extends ErrorHandlerRouteBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BillInquiryResponseDTO billInquiryResponseDTO;

    @Autowired
    private Bill billDetails;

    @Override
    public void configure() {

        from("direct:bill-inquiry")
                .routeId("bill-inquiry")
                .log("Received request for bill inquiry")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .process(exchange -> {
                    String billId = exchange.getIn().getHeader(BILL_ID).toString();
                    String billerID = exchange.getProperty(BILLER_ID).toString();
                    String billerName = exchange.getProperty(BILLER_NAME).toString();
                    BillInquiryResponseDTO billInquiryResponseDTO =
                                setResponseBody(exchange.getIn().getHeader(CLIENTCORRELATIONID).toString(),
                                        billId, billerID, billerName);
                    exchange.setProperty(BILL_INQUIRY_RESPONSE, billInquiryResponseDTO);
                    exchange.setProperty(BILL_FETCH_FAILED, false);
                    exchange.getIn().setBody(billInquiryResponseDTO);
                    logger.info("Bill Inquiry Response: " + billInquiryResponseDTO.toString());
                });
        from("direct:bill-inquiry-response")
                .routeId("bill-inquiry-response")
                        .log("Triggering callback for bill inquiry response")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .process(exchange -> {
                            String billInquiryResponseDTO =
                                    exchange.getProperty(BILL_INQUIRY_RESPONSE, String.class);
                            exchange.getIn().setBody(billInquiryResponseDTO);
                            logger.info("Bill Inquiry Response: " + billInquiryResponseDTO);
                        })
                .log(LoggingLevel.DEBUG, "Sending bill inquiry response to callback URL: ${exchangeProperty.X-CallbackURL}")
                .toD("${exchangeProperty.X-CallbackURL}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }

    private BillInquiryResponseDTO setResponseBody(String clientCorrelationId, String billId,String billerID, String billerName) {

        billDetails.setBillerId(billerID);
        billDetails.setBillerName(billerName);
        billDetails.setAmountonDueDate("1000");
        billDetails.setAmountAfterDueDate("1100");
        billDetails.setDueDate("2021-07-01");
        billDetails.setBillStatus("PAID");
        billInquiryResponseDTO.setBillDetails(billDetails);
        billInquiryResponseDTO.setBillId(billId);
        billInquiryResponseDTO.setCode(SUCCESS_RESPONSE_CODE.getValue());
        billInquiryResponseDTO.setReason(SUCCESS_RESPONSE_MESSAGE.getValue());
        billInquiryResponseDTO.setClientCorrelationId(clientCorrelationId);
        return billInquiryResponseDTO;
    }


}