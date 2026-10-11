package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_document_type_metadata")
public class PrintDocumentTypeMetadata extends ScopedEntity {
 private Long dictItemId;
 public Long getDictItemId(){return dictItemId;} public void setDictItemId(Long value){dictItemId=value;}
 private String businessType;
 public String getBusinessType(){return businessType;} public void setBusinessType(String value){businessType=value;}
 private String authorizationPolicyRef;
 public String getAuthorizationPolicyRef(){return authorizationPolicyRef;} public void setAuthorizationPolicyRef(String value){authorizationPolicyRef=value;}
 private String compatibleMainCatalogIdsJson;
 public String getCompatibleMainCatalogIdsJson(){return compatibleMainCatalogIdsJson;} public void setCompatibleMainCatalogIdsJson(String value){compatibleMainCatalogIdsJson=value;}
 private String fixedTypeFiltersJson;
 public String getFixedTypeFiltersJson(){return fixedTypeFiltersJson;} public void setFixedTypeFiltersJson(String value){fixedTypeFiltersJson=value;}
 private String metadataVersion;
 public String getMetadataVersion(){return metadataVersion;} public void setMetadataVersion(String value){metadataVersion=value;}
 private String metadataHash;
 public String getMetadataHash(){return metadataHash;} public void setMetadataHash(String value){metadataHash=value;}
}
