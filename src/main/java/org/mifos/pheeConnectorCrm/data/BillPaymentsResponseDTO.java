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

/*Sample response
{
  "RequestID": "915251236706",
  “code”: “00”
   “reason”: “Transaction Successful”
    "billId”: “123456789101112”,
  “status”: “ACK”
}

* */
@Component
public class BillPaymentsResponseDTO implements Serializable {

    private String code;
    private String reason;
    private String requestID;
    private String billId;
    private String paymentReferenceID;
    private String status;

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BillPaymentsResponseDTO{" +
                "code='" + code + '\'' +
                ", reason='" + reason + '\'' +
                ", requestID='" + requestID + '\'' +
                ", billId='" + billId + '\'' +
                ", paymentReferenceID='" + paymentReferenceID + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getPaymentReferenceID() {
        return paymentReferenceID;
    }

    public void setPaymentReferenceID(String paymentReferenceID) {
        this.paymentReferenceID = paymentReferenceID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }


}
