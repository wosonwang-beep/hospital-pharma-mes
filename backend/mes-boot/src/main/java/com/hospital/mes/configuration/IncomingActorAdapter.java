package com.hospital.mes.configuration;

import com.hospital.mes.qms.application.IncomingActorPort;
import com.hospital.mes.masterdata.application.MasterQueryService;
import com.hospital.mes.security.context.PlatformOrganizationResolver;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.common.exception.ComplianceException;
import java.time.Instant;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Binds the existing IAM deployment organization and real qualification producer. */
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingActorAdapter implements IncomingActorPort {
 private final SysUserMapper users;private final PlatformOrganizationResolver organizations;
 private final MasterQueryService master;private final Environment environment;
 public IncomingActorAdapter(SysUserMapper users,PlatformOrganizationResolver organizations,MasterQueryService master,Environment environment){
  this.users=users;this.organizations=organizations;this.master=master;this.environment=environment;
 }
 @Override public void requireUser(long orgId,long userId){
  var user=users.selectById(userId);
  if(orgId!=organizations.organizationId()||user==null||!Boolean.TRUE.equals(user.getEnabled()))
   throw new ComplianceException("QUALIFIED_ACTOR_REQUIRED","An enabled user in the deployment organization is required");
 }
 @Override public void requireQualified(long orgId,long userId,String operation){
  requireUser(orgId,userId);
  if(!Set.of("sample-execute","test-execute","test-review","qa-release").contains(operation))throw new IllegalArgumentException("Unknown qualification operation");
  String mapping=environment.getProperty("mes.qualification.required-codes."+operation,"");
  if(mapping.isBlank())throw new ComplianceException("QUALIFICATION_MAPPING_REQUIRED","Controlled qualification mapping is required");
  for(String code:mapping.split(",",-1)){
   if(code.isBlank())throw new ComplianceException("QUALIFICATION_MAPPING_REQUIRED","Qualification mapping contains an empty code");
   master.requireQualification(orgId,userId,code.strip(),Instant.now());
  }
 }
}
