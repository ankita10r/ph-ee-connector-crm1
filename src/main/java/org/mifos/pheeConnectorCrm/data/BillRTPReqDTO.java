package org.mifos.pheeConnectorCrm.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class BillRTPReqDTO implements Serializable {
    private String billRequestId;
    private String billId;
    private String status;
    private String rejectionReason;


}
