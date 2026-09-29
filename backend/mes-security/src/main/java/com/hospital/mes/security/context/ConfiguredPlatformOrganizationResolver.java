package com.hospital.mes.security.context;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;
@Component public class ConfiguredPlatformOrganizationResolver implements PlatformOrganizationResolver{
 private final long organizationId;public ConfiguredPlatformOrganizationResolver(@Value("${mes.platform.organization-id:0}")long id){organizationId=id;}
 @Override public long organizationId(){if(organizationId<=0)throw new IllegalStateException("mes.platform.organization-id must be configured");return organizationId;}
}
