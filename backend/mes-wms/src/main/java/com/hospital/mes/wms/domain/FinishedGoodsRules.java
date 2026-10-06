package com.hospital.mes.wms.domain;
import java.math.BigDecimal;
import com.hospital.mes.common.exception.ComplianceException;
public final class FinishedGoodsRules {
 private FinishedGoodsRules(){}
 public static void confirmReceipt(String batch,String request,long author,long actor){
  gate("PRODUCTION_COMPLETED".equals(batch),"FINISHED_PRODUCTION_REQUIRED","Production must be completed");
  gate("SUBMITTED".equals(request),"FINISHED_REQUEST_STATE_CONFLICT","Submitted request required");
  gate(author!=actor,"FINISHED_RECEIPT_INDEPENDENT","Independent warehouse receiver required");
 }
 public static void ship(String batch,String quality,String stock,boolean expired,BigDecimal available,BigDecimal amount){
  gate("QA_RELEASED".equals(batch)&&"RELEASED".equals(quality)&&"AVAILABLE".equals(stock),"FINISHED_SHIPMENT_NOT_RELEASED","Only released available finished stock can ship");
  gate(!expired,"FINISHED_SHIPMENT_EXPIRED","Expired finished stock cannot ship");
  gate(amount.signum()>0&&available.compareTo(amount)>=0,"FINISHED_SHIPMENT_STOCK_SHORT","Insufficient available stock");
 }
 private static void gate(boolean ok,String code,String reason){if(!ok)throw new ComplianceException(code,reason);}
}
