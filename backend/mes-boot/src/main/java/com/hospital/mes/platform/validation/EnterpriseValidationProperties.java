package com.hospital.mes.platform.validation;

import jakarta.annotation.PostConstruct;import java.time.Duration;import java.time.Period;import org.springframework.beans.factory.annotation.Value;import org.springframework.context.annotation.Profile;import org.springframework.stereotype.Component;

@Component @Profile("prod")
public class EnterpriseValidationProperties{
 private final String rpo,rto,retention,siteId,approvedTimeSource;
 public EnterpriseValidationProperties(@Value("${mes.continuity.rpo}")String rpo,@Value("${mes.continuity.rto}")String rto,
  @Value("${mes.compliance.gxp-retention}")String retention,@Value("${mes.deployment.site-id}")String siteId,
  @Value("${mes.time.approved-source}")String approvedTimeSource){this.rpo=rpo;this.rto=rto;this.retention=retention;this.siteId=siteId;this.approvedTimeSource=approvedTimeSource;}
 @PostConstruct public void validate(){try{Duration a=Duration.parse(required(rpo,"RPO")),b=Duration.parse(required(rto,"RTO"));Period p=Period.parse(required(retention,"GxP retention"));if(a.isZero()||a.isNegative()||b.isZero()||b.isNegative()||p.isZero()||p.isNegative())throw new IllegalStateException("Enterprise validation intervals must be positive");required(siteId,"site ID");required(approvedTimeSource,"approved time source");}catch(java.time.DateTimeException e){throw new IllegalStateException("Enterprise validation configuration has invalid ISO-8601 syntax",e);}}
 private static String required(String value,String name){if(value==null||value.isBlank())throw new IllegalStateException(name+" is required for production validation");return value;}
}
