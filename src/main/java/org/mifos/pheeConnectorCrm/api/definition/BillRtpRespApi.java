package org.mifos.pheeConnectorCrm.api.definition;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mifos.pheeConnectorCrm.data.BillRTPReqDTO;
import org.mifos.pheeConnectorCrm.data.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.CLIENTCORRELATIONID;
import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.PLATFORM_TENANT;

@Tag(name = "GOV")
public interface BillRtpRespApi {

    @Operation(
            summary = "Bill RTP Resp API from PBB to Bill Agg")
    @PostMapping("/billTransferRequests")
    ResponseEntity<ResponseDTO> billRTPResp(@RequestHeader(value=PLATFORM_TENANT) String tenantId,
                                           @RequestHeader(value=CLIENTCORRELATIONID) String correlationId,
                                           @RequestParam(value = "X-Biller-Id") String billerId,
                                           @RequestBody BillRTPReqDTO billRTPReqDTO)
            throws ExecutionException, InterruptedException;
}