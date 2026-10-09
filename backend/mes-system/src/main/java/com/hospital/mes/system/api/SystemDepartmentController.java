package com.hospital.mes.system.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.system.application.IamAuditWriter;
import com.hospital.mes.system.application.IamMutationExecutor;
import com.hospital.mes.system.application.PageResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SystemDepartmentController {
    private static final Pattern CODE=Pattern.compile("[A-Z][A-Z0-9_]{1,63}");
    public record Department(String id,String code,String name,String organizationId,String organizationName,
        String parentId,String parentName,String leaderUserId,String leaderName,String phone,
        String description,int sortNo,String status,long version,long memberCount) {}
    public record DepartmentCommand(String code,String name,String organizationId,String parentId,
        String leaderUserId,String phone,String description,Integer sortNo,String status) {}
    public record DepartmentVersion(long version,DepartmentCommand payload) {}
    public record Organization(String id,String name,String code) {}
    public record Person(String id,String name,String username) {}
    public record UserDepartment(String userId,String departmentId,String departmentName,long version) {}
    public record DepartmentAssignment(String departmentId) {}

    private final JdbcTemplate jdbc;
    private final CurrentPlatformContextResolver contexts;
    private final TraceIdProvider traces;
    private final IamMutationExecutor mutations;
    private final IamAuditWriter audit;
    public SystemDepartmentController(JdbcTemplate jdbc,CurrentPlatformContextResolver contexts,
        TraceIdProvider traces,IamMutationExecutor mutations,IamAuditWriter audit) {
        this.jdbc=jdbc;this.contexts=contexts;this.traces=traces;this.mutations=mutations;this.audit=audit;
    }
    private <T> ApiResponse<T> ok(T data){return ApiResponse.success(data,traces.currentTraceId());}
    private long org(){return contexts.current().organizationId();}
    private long id(String s){
        try{long result=Long.parseLong(s);if(result>0)return result;}catch(Exception ignored){}
        throw new IllegalArgumentException("Invalid identifier");
    }
    private Long nullableId(String value){return value==null||value.isBlank()?null:id(value);}
    private void check(boolean condition,String error){if(!condition)throw new IllegalArgumentException(error);}
    private String cleaned(String value,int max,boolean required){
        String v=value==null?"":value.strip();
        check((!required||!v.isEmpty())&&v.length()<=max,"Required field invalid or too long");
        return v.isBlank()?null:v;
    }
    private int sort(Integer value){check(value==null||value>=0&&value<=999999,"Invalid order");return value==null?0:value;}
    private long version(String raw){return IamRequestVersion.parse(raw);}
    private void fresh(int affected){if(affected!=1)throw new ResourceConflictException("VERSION_CONFLICT","Department has changed, reload it");}
    private void existsOrganization(long id){
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM md_organization WHERE org_id=? AND id=? AND status='ACTIVE'",Integer.class,org(),id);
        check(count!=null&&count==1,"Organization must be active and belong to this tenant");
    }
    private void existsUser(Long id){
        if(id==null)return;
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE id=? AND enabled=TRUE",Integer.class,id);
        check(count!=null&&count==1,"Department leader must be an active user");
    }
    private static final String SELECT=
        "SELECT d.*,o.org_name organization_name,p.department_name parent_name,u.display_name leader_name,"+
        "(SELECT COUNT(*) FROM sys_user_department ud WHERE ud.org_id=d.org_id AND ud.department_id=d.id) member_count "+
        "FROM sys_department d JOIN md_organization o ON o.id=d.organization_id AND o.org_id=d.org_id "+
        "LEFT JOIN sys_department p ON p.id=d.parent_id AND p.org_id=d.org_id "+
        "LEFT JOIN sys_user u ON u.id=d.leader_user_id WHERE d.org_id=?";
    private Department from(ResultSet rs,int idx)throws SQLException{
        return new Department(Long.toString(rs.getLong("id")),rs.getString("department_code"),rs.getString("department_name"),
            Long.toString(rs.getLong("organization_id")),rs.getString("organization_name"),
            idStr(rs,"parent_id"),rs.getString("parent_name"),idStr(rs,"leader_user_id"),rs.getString("leader_name"),
            rs.getString("phone"),rs.getString("description"),rs.getInt("sort_no"),rs.getString("status"),
            rs.getLong("version_no"),rs.getLong("member_count"));
    }
    private String idStr(ResultSet rs,String name)throws SQLException{
        Long value=rs.getObject(name,Long.class);return value==null?null:Long.toString(value);
    }
    private Department required(long departmentId){
        return jdbc.query(SELECT+" AND d.id=?",this::from,org(),departmentId).stream()
            .findFirst().orElseThrow(()->new NoSuchElementException("Department not found"));
    }
    @GetMapping("/departments")
    @PreAuthorize("hasAuthority('iam:department:view')")
    public ApiResponse<PageResult<Department>> list(@RequestParam(defaultValue="0")int page,
        @RequestParam(defaultValue="20")int size,@RequestParam(required=false)String code,
        @RequestParam(required=false)String name,@RequestParam(required=false)String status){
        check(page>=0&&page<=100000&&size>=1&&size<=100,"Invalid pagination");
        String where=" AND (? IS NULL OR d.department_code LIKE CONCAT('%',?,'%')) "+
            "AND (? IS NULL OR d.department_name LIKE CONCAT('%',?,'%')) AND (? IS NULL OR d.status=?)";
        String c=blank(code),n=blank(name),s=blank(status);
        var args=new Object[]{org(),c,c,n,n,s,s};
        Long count=jdbc.queryForObject("SELECT COUNT(*) FROM sys_department d WHERE d.org_id=?"+where,Long.class,args);
        List<Department> records=jdbc.query(SELECT+where+" ORDER BY d.sort_no,d.id LIMIT ? OFFSET ?",this::from,
            org(),c,c,n,n,s,s,size,(long)page*size);
        return ok(new PageResult<>(records,count==null?0:count,page,size));
    }
    private String blank(String s){return s==null||s.isBlank()?null:s.strip();}
    @GetMapping("/departments/tree")
    @PreAuthorize("hasAuthority('iam:department:view')")
    public ApiResponse<List<Department>> tree(){
        return ok(jdbc.query(SELECT+" ORDER BY d.sort_no,d.id LIMIT 2000",this::from,org()));
    }
    @GetMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('iam:department:view')")
    public ApiResponse<Department> get(@PathVariable String id){return ok(required(id(id)));}
    @GetMapping("/department-organizations")
    @PreAuthorize("hasAuthority('iam:department:view')")
    public ApiResponse<List<Organization>> organizations(){
        return ok(jdbc.query("SELECT id,org_name,org_code FROM md_organization WHERE org_id=? AND status='ACTIVE' ORDER BY org_name LIMIT 200",
            (rs,i)->new Organization(Long.toString(rs.getLong(1)),rs.getString(2),rs.getString(3)),org()));
    }
    @GetMapping("/department-users")
    @PreAuthorize("hasAuthority('iam:department:view')")
    public ApiResponse<List<Person>> people(@RequestParam(required=false)String keyword){
        String q=blank(keyword);return ok(jdbc.query(
            "SELECT id,display_name,login_name FROM sys_user WHERE enabled=TRUE AND (? IS NULL OR display_name LIKE CONCAT('%',?,'%') OR login_name LIKE CONCAT('%',?,'%')) ORDER BY display_name LIMIT 100",
            (rs,i)->new Person(Long.toString(rs.getLong(1)),rs.getString(2),rs.getString(3)),q,q,q));
    }
    private void validate(DepartmentCommand r){
        check(r!=null,"Department data required");check(r.code()!=null&&CODE.matcher(r.code()).matches(),"Invalid department code");
        cleaned(r.name(),120,true);cleaned(r.phone(),40,false);cleaned(r.description(),500,false);sort(r.sortNo());
        check("ACTIVE".equals(r.status())||"INACTIVE".equals(r.status()),"Invalid status");
        existsOrganization(id(r.organizationId()));existsUser(nullableId(r.leaderUserId()));
    }
    private void parentValid(long tenantOrganization,Long parentId,Long ownId){
        if(parentId==null)return;
        Department parent=required(parentId);
        check("ACTIVE".equals(parent.status()),"Parent department must be active");
        check(Long.parseLong(parent.organizationId())==tenantOrganization,"Parent must belong to selected organization");
        if(ownId!=null){
            check(!parentId.equals(ownId),"Department cannot parent itself");
            Long next=parentId;
            for(int step=0;next!=null&&step<2000;step++){
                check(!next.equals(ownId),"Department hierarchy cycle is not allowed");
                Department row=required(next);
                next=nullableId(row.parentId());
                check(step<1999,"Department hierarchy too deep");
            }
        }
    }
    private long created(String sql,Object...args){
        var keys=new GeneratedKeyHolder();
        PreparedStatementCreator p=con->{var statement=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for(int i=0;i<args.length;i++)statement.setObject(i+1,args[i]);return statement;};
        jdbc.update(p,keys);
        check(keys.getKey()!=null,"Department insert failed");return keys.getKey().longValue();
    }
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('iam:department:create')")
    public ApiResponse<JsonNode> create(@RequestHeader("Idempotency-Key")String key,@RequestBody DepartmentCommand request){
        validate(request);var ctx=contexts.current();
        return ok(mutations.execute(ctx,"departmentCreate",key,request,"DEPARTMENT",
            ()->add(ctx,request,key),Department::id));
    }
    private Department add(CurrentPlatformContext ctx,DepartmentCommand r,String key){
        Long parent=nullableId(r.parentId());long organization=id(r.organizationId());
        parentValid(organization,parent,null);
        long newId=created("INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,leader_user_id,phone,description,sort_no,status,created_by,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            org(),organization,parent,r.code(),cleaned(r.name(),120,true),nullableId(r.leaderUserId()),
            cleaned(r.phone(),40,false),cleaned(r.description(),500,false),sort(r.sortNo()),r.status(),ctx.actorId(),ctx.actorId());
        Department after=required(newId);
        audit.append(ctx,"DEPARTMENT_CREATED","DEPARTMENT",after.id(),null,after,"System department maintenance",key);return after;
    }
    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('iam:department:update')")
    public ApiResponse<JsonNode> update(@PathVariable String id,@RequestHeader("If-Match")String ifMatch,
        @RequestHeader("Idempotency-Key")String key,@RequestBody DepartmentCommand request){
        validate(request);long departmentId=id(id),expected=version(ifMatch);var ctx=contexts.current();
        return ok(mutations.execute(ctx,"departmentUpdate",key,new DepartmentVersion(expected,request),"DEPARTMENT",
            ()->change(ctx,departmentId,expected,request,key),Department::id));
    }
    private Department change(CurrentPlatformContext ctx,long departmentId,long expected,DepartmentCommand r,String key){
        Department before=required(departmentId);
        check(before.code().equals(r.code()),"Department code cannot change");
        Long parent=nullableId(r.parentId());long organization=id(r.organizationId());
        parentValid(organization,parent,departmentId);
        Integer descendants=jdbc.queryForObject("SELECT COUNT(*) FROM sys_department WHERE org_id=? AND parent_id=?",Integer.class,org(),departmentId);
        Integer members=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_department WHERE org_id=? AND department_id=?",Integer.class,org(),departmentId);
        if(!before.organizationId().equals(r.organizationId()))
            check(descendants!=null&&descendants==0&&members!=null&&members==0,
                "Reassign child departments and users before moving a department to another organization");
        if(!"ACTIVE".equals(r.status())){
            check(members!=null&&members==0,"Reassign department members before disabling department");
            Integer activeChildren=jdbc.queryForObject("SELECT COUNT(*) FROM sys_department WHERE org_id=? AND parent_id=? AND status='ACTIVE'",Integer.class,org(),departmentId);
            check(activeChildren!=null&&activeChildren==0,"Disable active child departments first");
        }
        fresh(jdbc.update("UPDATE sys_department SET organization_id=?,parent_id=?,department_name=?,leader_user_id=?,phone=?,description=?,sort_no=?,status=?,updated_by=?,updated_at=CURRENT_TIMESTAMP(3),version_no=version_no+1 WHERE org_id=? AND id=? AND version_no=?",
            organization,parent,cleaned(r.name(),120,true),nullableId(r.leaderUserId()),cleaned(r.phone(),40,false),cleaned(r.description(),500,false),sort(r.sortNo()),r.status(),ctx.actorId(),org(),departmentId,expected));
        Department after=required(departmentId);
        audit.append(ctx,"DEPARTMENT_UPDATED","DEPARTMENT",after.id(),before,after,"System department maintenance",key);return after;
    }
    @GetMapping("/users/{userId}/department")
    @PreAuthorize("hasAnyAuthority('iam:user:view','iam:department:view')")
    public ApiResponse<UserDepartment> getAssignment(@PathVariable String userId){
        long user=id(userId);
        return ok(jdbc.query("SELECT ud.department_id,d.department_name,ud.version_no FROM sys_user_department ud JOIN sys_department d ON d.id=ud.department_id AND d.org_id=ud.org_id WHERE ud.org_id=? AND ud.user_id=?",
            (rs,i)->new UserDepartment(userId,Long.toString(rs.getLong(1)),rs.getString(2),rs.getLong(3)),org(),user)
            .stream().findFirst().orElse(new UserDepartment(userId,null,null,0)));
    }
    @PutMapping("/users/{userId}/department")
    @PreAuthorize("hasAuthority('iam:department:update') and hasAuthority('iam:user:update')")
    public ApiResponse<JsonNode> assign(@PathVariable String userId,@RequestHeader("Idempotency-Key")String key,
        @RequestBody DepartmentAssignment request){
        long user=id(userId);var ctx=contexts.current();
        check(request!=null,"Department assignment required");
        return ok(mutations.execute(ctx,"userDepartmentAssign",key,new java.util.AbstractMap.SimpleEntry<>(user,request),"USER_DEPARTMENT",
            ()->saveAssignment(ctx,user,request.departmentId(),key),UserDepartment::userId));
    }
    private UserDepartment saveAssignment(CurrentPlatformContext ctx,long user,String deptId,String key){
        existsUser(user);
        UserDepartment before=getAssignment(Long.toString(user)).data();
        Long dep=nullableId(deptId);
        if(dep!=null){Department department=required(dep);check("ACTIVE".equals(department.status()),"Department inactive");}
        if(dep==null){jdbc.update("DELETE FROM sys_user_department WHERE org_id=? AND user_id=?",org(),user);}
        else jdbc.update("INSERT INTO sys_user_department(org_id,user_id,department_id,created_by,updated_by) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE department_id=VALUES(department_id),updated_by=VALUES(updated_by),updated_at=CURRENT_TIMESTAMP(3),version_no=version_no+1",
            org(),user,dep,ctx.actorId(),ctx.actorId());
        UserDepartment after=getAssignment(Long.toString(user)).data();
        audit.append(ctx,"USER_DEPARTMENT_ASSIGNED","USER_DEPARTMENT",Long.toString(user),before,after,"System user department assignment",key);
        return after;
    }
}
