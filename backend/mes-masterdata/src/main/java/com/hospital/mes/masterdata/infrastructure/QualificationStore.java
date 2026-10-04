package com.hospital.mes.masterdata.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QualificationStore extends ScopedStore<QualificationEntity> {
 public QualificationStore(QualificationMapper mapper){super(mapper,List.of("qualification_code"),Map.of("status","status","userId","user_id","qualificationCode","qualification_code"));}
}
