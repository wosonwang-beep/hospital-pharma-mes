package com.hospital.mes.masterdata.application;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.masterdata.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.math.BigDecimal;
import java.time.Instant;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MasterQueryService {
 private final UnitStore units;private final UnitConversionMapper conversions;private final QualificationMapper qualifications;
 public MasterQueryService(UnitStore units,UnitConversionMapper conversions,QualificationMapper qualifications){this.units=units;this.conversions=conversions;this.qualifications=qualifications;}
 public record UnitReference(String id,String unitCode,String dimension,int scale,long versionNo){}
 public record ConversionEvidence(Long conversionId,MasterRules.Conversion conversion){}
 public ConversionEvidence conversionEvidence(long org,long from,long to,Long material,BigDecimal amount){
  var a=units.get(org,from);var b=units.get(org,to);if(from==to)return new ConversionEvidence(null,MasterRules.convert(amount,a.getDimension(),b.getDimension(),BigDecimal.ONE,null,b.getScale()));
  UnitConversionEntity row=null;
  if(material!=null)row=conversions.selectOne(new QueryWrapper<UnitConversionEntity>().eq("org_id",org).eq("from_unit_id",from).eq("to_unit_id",to).eq("material_id",material));
  if(row==null)row=conversions.selectOne(new QueryWrapper<UnitConversionEntity>().eq("org_id",org).eq("from_unit_id",from).eq("to_unit_id",to).isNull("material_id"));
  return new ConversionEvidence(row==null?null:row.getId(),MasterRules.convert(amount,a.getDimension(),b.getDimension(),row==null?null:row.getFactor(),row==null?null:row.getMaterialId(),b.getScale()));
 }
 public UnitReference unit(long org,long id){var e=units.get(org,id);return new UnitReference(e.getId().toString(),e.getUnitCode(),e.getDimension(),e.getScale(),e.getVersionNo());}
 public String unitName(long org,long id){return units.get(org,id).getUnitName();}
 public MasterRules.Conversion convert(long org,long from,long to,Long material,BigDecimal amount){
  var a=units.get(org,from);var b=units.get(org,to);if(from==to)return MasterRules.convert(amount,a.getDimension(),b.getDimension(),BigDecimal.ONE,null,b.getScale());
  UnitConversionEntity row=null;
  if(material!=null)row=conversions.selectOne(new QueryWrapper<UnitConversionEntity>().eq("org_id",org).eq("from_unit_id",from).eq("to_unit_id",to).eq("material_id",material));
  if(row==null)row=conversions.selectOne(new QueryWrapper<UnitConversionEntity>().eq("org_id",org).eq("from_unit_id",from).eq("to_unit_id",to).isNull("material_id"));
  return MasterRules.convert(amount,a.getDimension(),b.getDimension(),row==null?null:row.getFactor(),row==null?null:row.getMaterialId(),b.getScale());
 }
 public void requireQualification(long org,long user,String code,Instant at){
  qualificationEvidence(org,user,code,at);
 }
 public record QualificationEvidence(String id,long versionNo,String code,String userId,String status,java.time.LocalDate validFrom,java.time.LocalDate validTo){}
 public QualificationEvidence qualificationEvidence(long org,long user,String code,Instant at){
  var rows=qualifications.selectList(new QueryWrapper<QualificationEntity>().eq("org_id",org).eq("user_id",user).eq("qualification_code",code).eq("status","ACTIVE"));
  for(var e:rows){try{MasterRules.qualification(e.getStatus(),e.getValidFrom(),e.getValidTo(),at);return new QualificationEvidence(e.getId().toString(),e.getVersionNo(),e.getQualificationCode(),e.getUserId().toString(),e.getStatus(),e.getValidFrom(),e.getValidTo());}catch(MasterGateException ignored){}}
  throw new MasterGateException("QUALIFICATION_REQUIRED","OBTAIN_VALID_QUALIFICATION");
 }
}
