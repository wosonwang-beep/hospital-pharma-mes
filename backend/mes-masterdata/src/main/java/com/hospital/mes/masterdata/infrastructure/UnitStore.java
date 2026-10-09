package com.hospital.mes.masterdata.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class UnitStore extends ScopedStore<UnitEntity> {
 public UnitStore(UnitMapper mapper){super(false,mapper,List.of("unit_code","unit_name","scale"),Map.of("dimension","dimension"));}
}
