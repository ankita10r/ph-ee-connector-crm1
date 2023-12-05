package org.mifos.pheeConnectorCrm.service;


import org.json.JSONObject;
import org.mifos.pheeConnectorCrm.data.BillPaymentsReqDTO;
import org.mifos.pheeConnectorCrm.data.BillPaymentsResponseDTO;
import org.mifos.pheeConnectorCrm.utils.BillPayEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.CLIENTCORRELATIONID;
import static org.mifos.pheeConnectorCrm.zeebe.ZeebeVariables.TENANT_ID;

@Service
public class BillPaymentsService {

    @Autowired
    private BillPaymentsResponseDTO billPaymentsResponseDTO;


    String transactionId;

    public BillPaymentsResponseDTO billPayments(String tenantId, String correlationId, String payerFspId, BillPaymentsReqDTO body) {

        billPaymentsResponseDTO.setBillId(body.getBillId());
        billPaymentsResponseDTO.setCode(BillPayEnum.SUCCESS_RESPONSE_CODE.toString());
        billPaymentsResponseDTO.setReason(BillPayEnum.SUCCESS_RESPONSE_MESSAGE.toString());
        billPaymentsResponseDTO.setStatus(BillPayEnum.SUCCESS_STATUS.toString());
        billPaymentsResponseDTO.setRequestID(correlationId);
        return billPaymentsResponseDTO;
    }

}
