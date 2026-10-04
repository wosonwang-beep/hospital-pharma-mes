package com.hospital.mes.masterdata.application;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.time.*;
import java.util.*;
import org.springframework.beans.BeanWrapperImpl;
public class ScopedStore<E extends ScopedEntity> {
    protected final BaseMapper<E> mapper;
    private final List<String> columns;
    private final Map<String,String> filters;
    public ScopedStore(BaseMapper<E> mapper,List<String> columns,Map<String,String> filters){this.mapper=mapper;this.columns=columns;this.filters=filters;}
    public E get(long org,long id){E row=mapper.selectOne(new QueryWrapper<E>().eq("org_id",org).eq("id",id)); if(row==null)throw new NoSuchElementException("Resource not found");return row;}
    public E lock(long org,long id){
        try {E row=mapper.selectOne(new QueryWrapper<E>().eq("org_id",org).eq("id",id).last("FOR UPDATE"));if(row==null)throw new NoSuchElementException("Resource not found");return row;}
        catch(RuntimeException ex){
            throw translateConcurrency(ex);
        }
    }
    public static RuntimeException translateConcurrency(RuntimeException ex){
        for(Throwable cause=ex;cause!=null;cause=cause.getCause())if(cause instanceof java.sql.SQLException sql && (sql.getErrorCode()==1020 || "40001".equals(sql.getSQLState())))return new ResourceConflictException("CONCURRENT_MODIFICATION","Concurrent record change; reload and retry");
        return ex;
    }
    public record PageData<T>(List<T> items,long total,int page,int size){}
    public PageData<E> list(long org,int page,int size,String keyword,Map<String,String> params){
        if(page<0 || page>1000000 || size<1 || size>100)throw new IllegalArgumentException("Invalid pagination");
        QueryWrapper<E> q=new QueryWrapper<E>().eq("org_id",org);
        if(keyword!=null && !keyword.isBlank()){
            if(keyword.length()>200)throw new IllegalArgumentException("Keyword too long");
            q.and(w->{boolean first=true;for(String col:columns){if(!first)w.or();w.like(col,keyword.strip());first=false;}});
        }
        filters.forEach((key,col)->{String val=params.get(key);if(val!=null&&!val.isBlank())filter(q,key,col,val);});
        long count=mapper.selectCount(q);String sort=params.getOrDefault("sort","id,desc");String[] parts=sort.split(",");
        String col=parts[0].replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);
        Set<String> sortable=new HashSet<>(columns);sortable.addAll(filters.values());sortable.addAll(List.of("id","updated_at","version_no"));
        if(!sortable.contains(col) || parts.length!=2 || !List.of("asc","desc").contains(parts[1]))throw new IllegalArgumentException("Invalid sort");
        q.orderBy(true,parts[1].equals("asc"),col);if(!col.equals("id"))q.orderByAsc("id");
        q.last("LIMIT "+((long)page*size)+","+size);return new PageData<>(mapper.selectList(q),count,page,size);
    }
    protected void filter(QueryWrapper<E> query,String key,String column,String value){if(key.equals("calibrationDueDate"))query.le(column,LocalDate.parse(value));else query.eq(column,value);}
    public long count(long org,String col,Object value,String status){var q=new QueryWrapper<E>().eq("org_id",org).eq(col,value);if(status!=null)q.eq("status",status);return mapper.selectList(q.last("FOR UPDATE")).size();}
    public void insert(E e,long org,long actor){var now=LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);e.setOrgId(org);e.setCreatedBy(actor);e.setUpdatedBy(actor);e.setCreatedAt(now);e.setUpdatedAt(now);e.setVersionNo(0L);mapper.insert(e);}
    public void update(E e,long expected,long actor,List<String> fields){
        e.setUpdatedBy(actor);e.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));e.setVersionNo(expected+1);
        var u=new UpdateWrapper<E>().eq("org_id",e.getOrgId()).eq("id",e.getId()).eq("version_no",expected);
        var bean=new BeanWrapperImpl(e);
        for(String f:fields)u.set(f.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT),bean.getPropertyValue(f));
        u.set("updated_by",actor).set("updated_at",e.getUpdatedAt()).set("version_no",expected+1);
        if(mapper.update(null,u)!=1)throw new ResourceConflictException("VERSION_CONFLICT","Record changed; reload before retrying");
    }
}
