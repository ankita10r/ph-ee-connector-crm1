package org.mifos.pheeConnectorCrm.api.definition;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mifos.pheeConnectorCrm.data.BillInquiryResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.*;

@Tag(name = "GOV")
public interface BillInquiryApi   {

    @Operation(
            summary = "Bill Fetch API from PBB to Bill Agg")
    @GetMapping("/bills/{billId}")
    BillInquiryResponseDTO billInquiry(@RequestHeader(value=PLATFORM_TENANT) String tenantId,
                                                       @RequestHeader(value=CLIENTCORRELATIONID) String correlationId,
                                                       @RequestHeader(value = PAYER_FSP)
                                                               String payerFspId,
                                                       @PathVariable(value = BILL_ID) String billId,
                                                       @RequestParam(value = FIELDS,defaultValue = "inquiry") String field)
            throws ExecutionException, InterruptedException, JsonProcessingException;
}