package org.mifos.pheeConnectorCrm.api.definition;



import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mifos.pheeConnectorCrm.data.BillInquiryResponseDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Tag(name = "GOV")
public interface BillPaymentsApi {

    @Operation(
            summary = "Bill Payments API from Payer FSP to PBB")
    @PostMapping("/paymentNotifications")
    BillPaymentsResponseDTO billPayments(@RequestHeader(value = PLATFORM_TENANT) String tenantId,
                                                         @RequestHeader(value = CLIENTCORRELATIONID) String correlationId,
                                                         @RequestHeader(value = PAYER_FSP)
                                                                String payerFspId,
                                                         @RequestBody BillPaymentsReqDTO body)
            throws ExecutionException, InterruptedException, JsonProcessingException;
}