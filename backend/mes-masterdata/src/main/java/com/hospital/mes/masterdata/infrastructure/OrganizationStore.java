package com.hospital.mes.masterdata.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class OrganizationStore extends ScopedStore<OrganizationEntity> {
 public OrganizationStore(OrganizationMapper mapper){super(false,mapper,List.of("org_code","org_name"),Map.of("status","status","orgType","org_type","parentId","parent_id"));}
}
