package com.hospital.mes.system.application;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** CSV import of NEW dictionary types only; no existing type, item or business row is overwritten. */
@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class DictionaryImportService {
    public record Input(String code,String name,String kind,String structure,String description,Integer sortNo,String status) {}
    public record PreviewRow(int line,String code,String name,String result,String message) {}
    public record Preview(List<PreviewRow> items,int accepted,int rejected) {}
    public record Imported(String id,String code) {}
    public record Receipt(List<Imported> imported,int importedCount) {}

    private static final Pattern CODE=Pattern.compile("[A-Z][A-Z0-9_]{1,63}");
    private static final int LIMIT=200;
    private final JdbcTemplate jdbc;
    private final CurrentPlatformContextResolver contexts;
    private final IamAuditWriter audit;
    public DictionaryImportService(JdbcTemplate jdbc,CurrentPlatformContextResolver contexts,IamAuditWriter audit){
        this.jdbc=jdbc;this.contexts=contexts;this.audit=audit;
    }
    public Preview preview(List<Input> rows){
        if(rows==null||rows.isEmpty()||rows.size()>LIMIT)
            throw new IllegalArgumentException("Import must contain 1 to 200 dictionary rows");
        CurrentPlatformContext context=contexts.current();
        Set<String> seen=new HashSet<>();
        List<PreviewRow> preview=new ArrayList<>();
        int accepted=0;
        for(int i=0;i<rows.size();i++){
            Input row=rows.get(i);String code=row==null||row.code()==null?"":row.code().strip();
            String err=validate(row);
            if(err==null&&!seen.add(code))err="文件内字典 CODE 重复";
            if(err==null){
                Integer exists=jdbc.queryForObject("SELECT COUNT(*) FROM sys_dict_type WHERE org_id=? AND dict_code=?",
                    Integer.class,context.organizationId(),code);
                if(exists!=null&&exists>0)err="数据库已存在该字典 CODE";
            }
            boolean ok=err==null;if(ok)accepted++;
            preview.add(new PreviewRow(i+2,code,row==null?null:row.name(),ok?"PASS":"CONFLICT",ok?"可导入":err));
        }
        return new Preview(List.copyOf(preview),accepted,rows.size()-accepted);
    }
    private String validate(Input row){
        if(row==null)return "缺少字典数据";
        if(row.code()==null||!CODE.matcher(row.code().strip()).matches())return "字典 CODE 格式错误";
        if(row.name()==null||row.name().isBlank()||row.name().strip().length()>100)return "字典名称不能为空或超过 100 字";
        if(!"SYSTEM".equals(row.kind())&&!"BUSINESS".equals(row.kind()))return "字典类型无效";
        if(!"FLAT".equals(row.structure())&&!"TREE".equals(row.structure()))return "结构类型无效";
        if(!"ACTIVE".equals(row.status())&&!"INACTIVE".equals(row.status()))return "启停状态无效";
        if(row.description()!=null&&row.description().length()>500)return "描述超过 500 字";
        if(row.sortNo()!=null&&(row.sortNo()<0||row.sortNo()>999999))return "排序值无效";
        return null;
    }
    /** Called inside IamMutationExecutor transaction after idempotency begin. */
    public Receipt importRows(List<Input> rows,String key){
        Preview preview=preview(rows);
        if(preview.rejected()>0)throw new ResourceConflictException("DICTIONARY_IMPORT_CONFLICT",
            "字典导入内容已变更或存在冲突，请重新预检");
        CurrentPlatformContext context=contexts.current();
        List<Imported> imported=new ArrayList<>();
        for(Input row:rows){
            jdbc.update("INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?)",
                context.organizationId(),row.code().strip(),row.name().strip(),row.kind(),row.structure(),
                row.description(),row.sortNo()==null?0:row.sortNo(),row.status(),context.actorId(),context.actorId());
            Long id=jdbc.queryForObject("SELECT id FROM sys_dict_type WHERE org_id=? AND dict_code=?",
                Long.class,context.organizationId(),row.code().strip());
            Imported created=new Imported(Long.toString(id),row.code().strip());imported.add(created);
            audit.append(context,"DICTIONARY_TYPE_IMPORTED","DICT_TYPE",created.id(),null,row,
                "System dictionary CSV import",key);
        }
        return new Receipt(List.copyOf(imported),imported.size());
    }
}
