package com.hospital.mes.ebr.api;

import com.hospital.mes.ebr.application.EbrService;
import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/ebr")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrController {
    private final EbrService service;private final TraceIdProvider traces;
    public EbrController(EbrService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
    private <T>ApiResponse<T> response(T data){return ApiResponse.success(data,traces.currentTraceId());}
    @GetMapping("/templates") public Object list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> filters){return response(service.list(page,size,keyword,filters));}
    @GetMapping("/templates/{id}") public Object detail(@PathVariable("id")String id){return response(service.get(id));}
    @PostMapping("/templates") public Object create(@RequestBody Create request,@RequestHeader("Idempotency-Key")String key){return response(service.create(request,key));}
    @PutMapping("/templates/{id}") public Object save(@PathVariable("id")String id,@RequestBody Save request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.save(id,request,version,key));}
    @PostMapping("/templates/{id}/versions") public Object version(@PathVariable("id")String id,@RequestBody VersionCreate request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.version(id,request,version,key));}
    @PostMapping("/versions/{id}/lint") public Object lint(@PathVariable("id")String id,@RequestBody Command request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.command(id,"LINT",request,version,key));}
    @PostMapping("/versions/{id}/submit") public Object submit(@PathVariable("id")String id,@RequestBody Command request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.command(id,"SUBMIT",request,version,key));}
    @PostMapping("/versions/{id}/approve") public Object approve(@PathVariable("id")String id,@RequestBody Command request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.command(id,"APPROVE",request,version,key));}
    @PostMapping("/versions/{id}/publish") public Object publish(@PathVariable("id")String id,@RequestBody Command request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.command(id,"PUBLISH",request,version,key));}
    @PostMapping("/versions/{id}/simulate") public Object simulate(@PathVariable("id")String id,@RequestBody Simulation request,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.simulate(id,request,version,key));}
    @GetMapping("/versions/{id}/compare") public Object compare(@PathVariable("id")String id,@RequestParam("otherVersionId")String other){return response(service.compare(id,other));}
}
