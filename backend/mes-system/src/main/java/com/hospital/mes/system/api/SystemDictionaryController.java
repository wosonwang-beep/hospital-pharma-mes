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
import com.hospital.mes.system.application.DictionaryImportService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix="spring.datasource", name="url")
public class SystemDictionaryController {
    private static final Pattern CODE=Pattern.compile("[A-Z][A-Z0-9_]{1,63}");
    private static final Pattern ITEM_CODE=Pattern.compile("[A-Za-z0-9_\\-]{1,80}");
    public record DictType(String id,String code,String name,String kind,String structure,String description,
                           int sortNo,String status,long version,long itemCount,String createdAt,String updatedAt) {}
    public record DictItem(String id,String dictTypeId,String parentId,String code,String label,
                           String description,int sortNo,String status,boolean isDefault,long version) {}
    public record Option(String value,String label,String parentId,boolean disabled) {}
    public record TypeRequest(String code,String name,String kind,String structure,String description,Integer sortNo,String status) {}
    public record ItemRequest(String code,String label,String description,String parentId,Integer sortNo,String status,Boolean isDefault) {}
    public record Revision(String id,long version,Object request) {}
    private final JdbcTemplate db;
    private final CurrentPlatformContextResolver contexts;
    private final TraceIdProvider traces;
    private final IamMutationExecutor mutations;
    private final IamAuditWriter audit;
    private final DictionaryImportService imports;

    public SystemDictionaryController(JdbcTemplate db,CurrentPlatformContextResolver contexts,
        TraceIdProvider traces,IamMutationExecutor mutations,IamAuditWriter audit,DictionaryImportService imports){
        this.db=db;this.contexts=contexts;this.traces=traces;this.mutations=mutations;this.audit=audit;this.imports=imports;
    }
    private <T> ApiResponse<T> ok(T data){return ApiResponse.success(data,traces.currentTraceId());}
    private long org(){return contexts.current().organizationId();}
    private long id(String raw){
        try{long value=Long.parseLong(raw);if(value>0)return value;}catch(NumberFormatException ignored){}
        throw new IllegalArgumentException("Invalid identifier");
    }
    private int size(Integer v,int max){return v==null?20:Math.max(1,Math.min(max,v));}
    private void verify(boolean flag,String message){if(!flag)throw new IllegalArgumentException(message);}
    private String name(String value){verify(value!=null&&!value.isBlank()&&value.strip().length()<=100,"Name is required");return value.strip();}
    private String status(String s){verify("ACTIVE".equals(s)||"INACTIVE".equals(s),"Unknown status");return s;}
    private void code(String s,Pattern regex){verify(s!=null&&regex.matcher(s).matches(),"Invalid dictionary code");}
    private String desc(String d){verify(d==null||d.length()<=500,"Description too long");return d;}
    private int sort(Integer n){verify(n==null||n>=0&&n<=999999,"Invalid sort order");return n==null?0:n;}
    private void notStale(int changed){if(changed!=1)throw new ResourceConflictException("VERSION_CONFLICT","Record changed, please reload");}
    private DictType type(ResultSet rs,int row)throws SQLException{
        return new DictType(Long.toString(rs.getLong("id")),rs.getString("dict_code"),rs.getString("dict_name"),
            rs.getString("dict_kind"),rs.getString("structure_type"),rs.getString("description"),
            rs.getInt("sort_no"),rs.getString("status"),rs.getLong("version_no"),rs.getLong("item_count"),
            rs.getTimestamp("created_at").toLocalDateTime().toString(),rs.getTimestamp("updated_at").toLocalDateTime().toString());
    }
    private DictItem item(ResultSet rs,int row)throws SQLException{
        Long parent=rs.getObject("parent_id",Long.class);
        return new DictItem(Long.toString(rs.getLong("id")),Long.toString(rs.getLong("dict_type_id")),
            parent==null?null:Long.toString(parent),rs.getString("item_code"),rs.getString("item_label"),
            rs.getString("description"),rs.getInt("sort_no"),rs.getString("status"),
            rs.getBoolean("is_default"),rs.getLong("version_no"));
    }
    private static final String TYPE_SELECT=
        "SELECT t.*, (SELECT COUNT(*) FROM sys_dict_item i WHERE i.org_id=t.org_id AND i.dict_type_id=t.id) item_count "+
        "FROM sys_dict_type t WHERE t.org_id=?";
    private static final String ITEM_SELECT="SELECT * FROM sys_dict_item WHERE org_id=? AND dict_type_id=?";
    private DictType requireType(long id){return db.query(TYPE_SELECT+" AND t.id=?",this::type,org(),id)
        .stream().findFirst().orElseThrow(()->new NoSuchElementException("Dictionary not found"));}
    private DictItem requireItem(long dictId,long itemId){return db.query(ITEM_SELECT+" AND id=?",this::item,org(),dictId,itemId)
        .stream().findFirst().orElseThrow(()->new NoSuchElementException("Dictionary item not found"));}
    private long generated(String sql,Object...params){
        GeneratedKeyHolder holder=new GeneratedKeyHolder();
        PreparedStatementCreator creator=con->{var ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for(int i=0;i<params.length;i++)ps.setObject(i+1,params[i]);return ps;};
        db.update(creator,holder);verify(holder.getKey()!=null,"Insert failed");return holder.getKey().longValue();
    }
    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('iam:dict:view')")
    public ApiResponse<PageResult<DictType>> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(required=false) String code,@RequestParam(required=false) String name,
        @RequestParam(required=false) String kind,@RequestParam(required=false) String status,
        @RequestParam(required=false) String createdFrom,@RequestParam(required=false) String createdTo){
        verify(page>=0&&page<=100000,"Invalid page");int limit=size(size,100);
        String where=" WHERE t.org_id=? AND (? IS NULL OR t.dict_code LIKE CONCAT('%',?,'%')) "+
            "AND (? IS NULL OR t.dict_name LIKE CONCAT('%',?,'%')) AND (? IS NULL OR t.dict_kind=?) AND (? IS NULL OR t.status=?) "+
            "AND (? IS NULL OR t.created_at>=?) AND (? IS NULL OR t.created_at<DATE_ADD(?, INTERVAL 1 DAY))";
        String q1=blank(code),q2=blank(name),q3=blank(kind),q4=blank(status);
        String from=date(createdFrom),to=date(createdTo);
        Object[] args={org(),q1,q1,q2,q2,q3,q3,q4,q4,from,from,to,to};
        Long total=db.queryForObject("SELECT COUNT(*) FROM sys_dict_type t"+where,Long.class,args);
        List<DictType> list=db.query(TYPE_SELECT.substring(0,TYPE_SELECT.length()-(" WHERE t.org_id=?").length())+
            where+" ORDER BY t.sort_no,t.id LIMIT ? OFFSET ?",this::type,concat(args,limit,(long)page*limit));
        return ok(new PageResult<>(list,total==null?0:total,page,limit));
    }
    private Object[] concat(Object[] arr,Object...extra){var all=new Object[arr.length+extra.length];System.arraycopy(arr,0,all,0,arr.length);System.arraycopy(extra,0,all,arr.length,extra.length);return all;}
    private String blank(String s){return s==null||s.isBlank()?null:s.strip();}
    private String date(String value){if(value==null||value.isBlank())return null;try{return java.time.LocalDate.parse(value.strip()).toString();}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Invalid date",e);}}
    @PostMapping("/dictionaries/import/preview")
    @PreAuthorize("hasAuthority('iam:dict:create')")
    public ApiResponse<DictionaryImportService.Preview> previewImport(@RequestBody List<DictionaryImportService.Input> rows){
        return ok(imports.preview(rows));
    }
    @PostMapping("/dictionaries/import")
    @PreAuthorize("hasAuthority('iam:dict:create')")
    public ApiResponse<JsonNode> importCsv(@RequestHeader("Idempotency-Key")String key,
        @RequestBody List<DictionaryImportService.Input> rows){
        CurrentPlatformContext c=contexts.current();
        return ok(mutations.execute(c,"dictTypeImport",key,rows,"DICT_IMPORT",
            ()->imports.importRows(rows,key),result->"BATCH:"+result.importedCount()));
    }
    @GetMapping("/dictionaries/{id}")
    @PreAuthorize("hasAuthority('iam:dict:view')")
    public ApiResponse<DictType> get(@PathVariable String id){return ok(requireType(id(id)));}
    @GetMapping("/dictionaries/{id}/items")
    @PreAuthorize("hasAuthority('iam:dict:view')")
    public ApiResponse<List<DictItem>> items(@PathVariable String id){
        long typeId=id(id);requireType(typeId);
        return ok(db.query(ITEM_SELECT+" ORDER BY sort_no,id",this::item,org(),typeId));
    }
    @GetMapping("/dictionary-options/{code}")
    public ApiResponse<List<Option>> options(@PathVariable String code,
        @RequestParam(required=false) String selectedValue){
        verify(code!=null&&CODE.matcher(code).matches(),"Invalid dictionary code");
        verify(selectedValue==null||selectedValue.length()<=80,"Invalid selected option");
        var types=db.query("SELECT id,status FROM sys_dict_type WHERE org_id=? AND dict_code=?",
            (rs,i)->Map.entry(rs.getLong(1),rs.getString(2)),org(),code);
        if(types.isEmpty())return ok(selectedValue==null||selectedValue.isBlank()?List.of():
            List.of(new Option(selectedValue,selectedValue+"（历史值）",null,true)));
        long typeId=types.get(0).getKey();
        boolean typeEnabled="ACTIVE".equals(types.get(0).getValue());
        var values=db.query("SELECT item_code,item_label,parent_id,status FROM sys_dict_item "+
            "WHERE org_id=? AND dict_type_id=? AND ((?=TRUE AND status='ACTIVE') OR item_code=?) ORDER BY sort_no,id",
            (rs,i)->{Long p=rs.getObject("parent_id",Long.class);String state=rs.getString("status");
                return new Option(rs.getString("item_code"),rs.getString("item_label"),
                    p==null?null:Long.toString(p),!typeEnabled||!"ACTIVE".equals(state));},
            org(),typeId,typeEnabled,selectedValue==null?"":selectedValue);
        if(selectedValue!=null&&!selectedValue.isBlank()&&values.stream().noneMatch(v->v.value().equals(selectedValue)))
            values.add(new Option(selectedValue,selectedValue+"（历史值）",null,true));
        return ok(values);
    }
    private void validateType(TypeRequest r){
        verify(r!=null,"Required dictionary");code(r.code(),CODE);name(r.name());
        verify("SYSTEM".equals(r.kind())||"BUSINESS".equals(r.kind()),"Invalid dictionary type");
        verify("FLAT".equals(r.structure())||"TREE".equals(r.structure()),"Invalid dictionary structure");
        status(r.status());sort(r.sortNo());desc(r.description());
    }
    @PostMapping("/dictionaries")
    @PreAuthorize("hasAuthority('iam:dict:create')")
    public ApiResponse<JsonNode> create(@RequestHeader("Idempotency-Key")String key,@RequestBody TypeRequest r){
        validateType(r);CurrentPlatformContext c=contexts.current();
        return ok(mutations.execute(c,"dictTypeCreate",key,r,"DICT_TYPE",()->createType(c,r,key),DictType::id));
    }
    private DictType createType(CurrentPlatformContext c,TypeRequest r,String key){
        long id=generated("INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?)",
            c.organizationId(),r.code(),name(r.name()),r.kind(),r.structure(),desc(r.description()),sort(r.sortNo()),r.status(),c.actorId(),c.actorId());
        DictType result=requireType(id);audit.append(c,"DICTIONARY_TYPE_CREATED","DICT_TYPE",result.id(),null,result,"System dictionary maintenance",key);return result;
    }
    @PutMapping("/dictionaries/{id}")
    @PreAuthorize("hasAuthority('iam:dict:update')")
    public ApiResponse<JsonNode> update(@PathVariable String id,@RequestHeader("If-Match")String match,
        @RequestHeader("Idempotency-Key")String key,@RequestBody TypeRequest r){
        long dictId=id(id),version=IamRequestVersion.parse(match);validateType(r);CurrentPlatformContext c=contexts.current();
        return ok(mutations.execute(c,"dictTypeUpdate",key,new Revision(id,version,r),"DICT_TYPE",
            ()->updateType(c,dictId,version,r,key),DictType::id));
    }
    private DictType updateType(CurrentPlatformContext c,long id,long version,TypeRequest r,String key){
        DictType before=requireType(id);
        verify(before.code().equals(r.code()),"Dictionary code is immutable");
        verify(before.kind().equals(r.kind()),"Dictionary type cannot be changed after creation");
        if(!before.structure().equals(r.structure())) {
            Long count=db.queryForObject("SELECT COUNT(*) FROM sys_dict_item WHERE org_id=? AND dict_type_id=?",Long.class,org(),id);
            verify(count!=null&&count==0,"Structure cannot change while items exist");
        }
        notStale(db.update("UPDATE sys_dict_type SET dict_name=?,structure_type=?,description=?,sort_no=?,status=?,updated_by=?,updated_at=CURRENT_TIMESTAMP(3),version_no=version_no+1 WHERE org_id=? AND id=? AND version_no=?",
            name(r.name()),r.structure(),desc(r.description()),sort(r.sortNo()),r.status(),c.actorId(),org(),id,version));
        DictType after=requireType(id);audit.append(c,"DICTIONARY_TYPE_UPDATED","DICT_TYPE",after.id(),before,after,"System dictionary maintenance",key);return after;
    }
    private void validateItem(ItemRequest r){
        verify(r!=null,"Required dictionary item");code(r.code(),ITEM_CODE);name(r.label());status(r.status());
        sort(r.sortNo());desc(r.description());
    }
    @PostMapping("/dictionaries/{id}/items")
    @PreAuthorize("hasAuthority('iam:dict:update')")
    public ApiResponse<JsonNode> createItem(@PathVariable String id,@RequestHeader("Idempotency-Key")String key,@RequestBody ItemRequest r){
        long type=id(id);validateItem(r);CurrentPlatformContext c=contexts.current();
        return ok(mutations.execute(c,"dictItemCreate",key,Map.of("dictId",type,"payload",r),"DICT_ITEM",()->addItem(c,type,r,key),DictItem::id));
    }
    private Long parent(DictType type,String id,Long self){
        if(id==null||id.isBlank())return null;
        verify("TREE".equals(type.structure()),"Flat dictionaries cannot have child items");
        long p=id(id);verify(self==null||self!=p,"Parent cannot be self");
        DictItem parent=requireItem(Long.parseLong(type.id()),p);
        for(int steps=0;steps<100&&parent.parentId()!=null;steps++){
            long next=Long.parseLong(parent.parentId());verify(self==null||self!=next,"Parent cycle forbidden");
            parent=requireItem(Long.parseLong(type.id()),next);
            if(steps==99)throw new IllegalArgumentException("Hierarchy too deep");
        }
        return p;
    }
    private DictItem addItem(CurrentPlatformContext c,long typeId,ItemRequest r,String key){
        DictType t=requireType(typeId);Long parent=parent(t,r.parentId(),null);
        long id=generated("INSERT INTO sys_dict_item(org_id,dict_type_id,parent_id,item_code,item_label,description,sort_no,status,is_default,created_by,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            org(),typeId,parent,r.code(),name(r.label()),desc(r.description()),sort(r.sortNo()),r.status(),Boolean.TRUE.equals(r.isDefault()),c.actorId(),c.actorId());
        if(Boolean.TRUE.equals(r.isDefault()))resetOtherDefaults(typeId,id);
        DictItem after=requireItem(typeId,id);audit.append(c,"DICTIONARY_ITEM_CREATED","DICT_ITEM",after.id(),null,after,"System dictionary maintenance",key);
        return after;
    }
    private void resetOtherDefaults(long typeId,long self){
        db.update("UPDATE sys_dict_item SET is_default=FALSE,updated_at=CURRENT_TIMESTAMP(3),version_no=version_no+1 WHERE org_id=? AND dict_type_id=? AND id<>? AND is_default=TRUE",
            org(),typeId,self);
    }
    @PutMapping("/dictionaries/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('iam:dict:update')")
    public ApiResponse<JsonNode> updateItem(@PathVariable String id,@PathVariable String itemId,
        @RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key,@RequestBody ItemRequest r){
        long type=id(id),item=id(itemId),version=IamRequestVersion.parse(match);
        validateItem(r);CurrentPlatformContext c=contexts.current();
        return ok(mutations.execute(c,"dictItemUpdate",key,new Revision(itemId,version,r),"DICT_ITEM",
            ()->changeItem(c,type,item,version,r,key),DictItem::id));
    }
    private DictItem changeItem(CurrentPlatformContext c,long typeId,long itemId,long version,ItemRequest r,String key){
        DictType t=requireType(typeId);DictItem before=requireItem(typeId,itemId);
        verify(before.code().equals(r.code()),"Referenced item codes are immutable");
        Long parent=parent(t,r.parentId(),itemId);
        notStale(db.update("UPDATE sys_dict_item SET item_label=?,description=?,parent_id=?,sort_no=?,status=?,is_default=?,updated_by=?,updated_at=CURRENT_TIMESTAMP(3),version_no=version_no+1 WHERE org_id=? AND dict_type_id=? AND id=? AND version_no=?",
            name(r.label()),desc(r.description()),parent,sort(r.sortNo()),status(r.status()),Boolean.TRUE.equals(r.isDefault()),c.actorId(),org(),typeId,itemId,version));
        if(Boolean.TRUE.equals(r.isDefault()))resetOtherDefaults(typeId,itemId);
        DictItem after=requireItem(typeId,itemId);audit.append(c,"DICTIONARY_ITEM_UPDATED","DICT_ITEM",after.id(),before,after,"System dictionary maintenance",key);return after;
    }
}
