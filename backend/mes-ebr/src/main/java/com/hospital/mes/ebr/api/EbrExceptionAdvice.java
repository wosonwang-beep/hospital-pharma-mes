package com.hospital.mes.ebr.api;

import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.masterdata.api.MasterExceptionAdvice;
@org.springframework.web.bind.annotation.RestControllerAdvice(basePackageClasses=EbrController.class)
@org.springframework.core.annotation.Order(-10)
public class EbrExceptionAdvice extends MasterExceptionAdvice {
    public EbrExceptionAdvice(TraceIdProvider traces){super(traces);}
}
