package com.hospital.mes.release.domain;
public record ArchiveCommand(Long versionNo,String archiveKind,String reason){
 public void validate(){if(versionNo==null||versionNo<0||archiveKind==null||!java.util.Set.of("REVIEW_COPY","FINAL").contains(archiveKind)||reason==null||reason.isBlank()||reason.length()>1000)throw new IllegalArgumentException("Valid archive version, kind and reason required");}
}
