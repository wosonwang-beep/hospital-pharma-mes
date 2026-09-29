package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.audit.signature.*;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MybatisSignatureRepository implements SignatureRepository{
    private final SignatureMapper mapper; public MybatisSignatureRepository(SignatureMapper mapper){this.mapper=mapper;}
    @Override public SignatureRecord insert(NewSignature s){
        SignatureEntity e=new SignatureEntity();var at=s.signedAt().atOffset(ZoneOffset.UTC).toLocalDateTime();
        e.setOrgId(s.organizationId());e.setCreatedBy(s.signerId());e.setCreatedAt(at);e.setUpdatedBy(s.signerId());e.setUpdatedAt(at);e.setVersionNo(0L);
        e.setSignerId(s.signerId());e.setMeaning(s.meaning().name());e.setObjectType(s.objectType());e.setObjectId(s.objectId());
        e.setRecordDigest(s.recordDigest());e.setSignedAt(at);e.setStatus(SignatureStatus.VALID.name());e.setAuthContextJson(s.authContextJson());e.setRevokedSignatureId(s.revokedSignatureId());
        if(mapper.insert(e)!=1)throw new IllegalStateException("signature insert failed");return domain(e);
    }
    @Override public SignatureRecord find(long org,long id){
        SignatureEntity e=mapper.selectOne(new LambdaQueryWrapper<SignatureEntity>().eq(SignatureEntity::getOrgId,org).eq(SignatureEntity::getId,id));
        if(e==null)throw new com.hospital.mes.common.exception.ValidationException("SIGNATURE_NOT_FOUND","Signature not found");return domain(e);
    }
    @Override public java.util.Optional<SignatureRecord> findLatest(long org,String objectType,String objectId,SignatureMeaning meaning){
        SignatureEntity e=mapper.selectOne(new LambdaQueryWrapper<SignatureEntity>()
            .eq(SignatureEntity::getOrgId,org).eq(SignatureEntity::getObjectType,objectType)
            .eq(SignatureEntity::getObjectId,objectId).eq(SignatureEntity::getMeaning,meaning.name())
            .orderByDesc(SignatureEntity::getId).last("LIMIT 1"));
        return java.util.Optional.ofNullable(e).map(MybatisSignatureRepository::domain);
    }
    @Override public java.util.List<SignatureRecord> findValid(long org,String objectType,String objectId){
        return mapper.selectList(new LambdaQueryWrapper<SignatureEntity>()
                .eq(SignatureEntity::getOrgId,org).eq(SignatureEntity::getObjectType,objectType)
                .eq(SignatureEntity::getObjectId,objectId).eq(SignatureEntity::getStatus,SignatureStatus.VALID.name())
                .orderByAsc(SignatureEntity::getId))
            .stream().map(MybatisSignatureRepository::domain).toList();
    }
    @Override public boolean invalidate(long org,long id,long version,long actor,Instant at,String reason){return mapper.invalidate(org,id,version,actor,at.atOffset(ZoneOffset.UTC).toLocalDateTime(),reason)==1;}
    private static SignatureRecord domain(SignatureEntity e){return new SignatureRecord(e.getId(),e.getOrgId(),e.getSignerId(),SignatureMeaning.valueOf(e.getMeaning()),
        e.getObjectType(),e.getObjectId(),e.getRecordDigest(),e.getSignedAt().toInstant(ZoneOffset.UTC),SignatureStatus.valueOf(e.getStatus()),
        e.getInvalidatedAt()==null?null:e.getInvalidatedAt().toInstant(ZoneOffset.UTC),e.getInvalidationReason(),e.getAuthContextJson(),e.getRevokedSignatureId(),e.getVersionNo());}
}
