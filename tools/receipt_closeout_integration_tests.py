from pathlib import Path
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes');f=p/'backend/mes-boot/src/test/java/com/hospital/mes/wms/integration/ReceiptSourceIntegrationIT.java';e=p/'docs/review/evidence/RECEIPT_CLOSEOUT_20261019'
s=f.read_text(encoding='utf-8');assert 'sourceAExemptReceiptUsesExistingReleaseEvent' not in s
(e/'ReceiptSourceIntegrationIT.before.java').write_bytes(f.read_bytes())
a=' @Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;';assert a in s
s=s.replace(a,a+'\n @Autowired com.hospital.mes.audit.attachment.application.AttachmentService fileService;\n @Autowired WmsAttachmentService fileLinks;')
extra='''
 @Test void sourceAExemptReceiptUsesExistingReleaseEvent() {
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE org_id=? AND id=?",testOrg,material);
  var r=service.createReceipt(body(List.of(item(reserve(key()),true)),true),key(),false);
  var result=service.confirmReceipt(r.path("id").asText(),new Command(0L,"按既有免验规则核对收货"),null,key());
  String lotId=result.path("items").get(0).path("materialLotId").asText();
  var lot=service.get("MaterialLot",lotId);
  assertThat(lot.path("requiresIncomingInspectionSnapshot").asBoolean()).isFalse();
  assertThat(lot.path("inventoryStatus").asText()).isEqualTo("AVAILABLE");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE org_id=? AND material_lot_id=? AND decision_source='SYSTEM_RULE'",Long.class,testOrg,lotId)).isEqualTo(1);
 }
 @Test void sourceAUncheckedExemptReceiptCannotCreateRelease() {
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE org_id=? AND id=?",testOrg,material);
  var r=service.createReceipt(body(List.of(item(reserve(key()),null)),null),key(),false);
  assertThatThrownBy(()->service.confirmReceipt(r.path("id").asText(),new Command(0L,"核对未完成不得接收入库"),null,key())).hasMessageContaining("check");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material_lot WHERE org_id=?",Long.class,testOrg)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE org_id=?",Long.class,testOrg)).isZero();
 }
 @Test void sourceATwoEvidenceFilesAreRetainedReplaySafeAndScoped() {
  permissions.addAll(Set.of("attachment:upload","attachment:view"));
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  String number=reserve(key());var r=service.createReceipt(body(List.of(item(number,true)),true),key(),false);
  String id=r.path("id").asText();var confirmed=service.confirmReceipt(id,new Command(0L,"逐批核对接收"),null,key());
  var before=service.get("Receipt",id).deepCopy();
  for(int n=1;n<=2;n++){
   byte[] content=("收货核对记录\\n内部物料批号："+number+"\\n生产批号：GL-20261008\\n文件序号："+n).getBytes(java.nio.charset.StandardCharsets.UTF_8);
   var file=fileService.upload("收货核对记录"+n+".txt","text/plain",content,key());
   String linkKey=key();var command=new WmsAttachmentService.Link(confirmed.path("versionNo").asLong(),file.id(),"OTHER","保存收货核对记录");
   String header="\\\""+confirmed.path("versionNo").asLong()+"\\\"";
   var linked=fileLinks.link(id,command,header,linkKey);
   assertThat(fileLinks.link(id,command,header,linkKey)).isEqualTo(linked);
   assertThat(fileLinks.download(id,file.id()).content()).containsExactly(content);
  }
  assertThat(fileLinks.list(id)).hasSize(2);
  assertThat(service.get("Receipt",id)).isEqualTo(before);
  var fileId=fileLinks.list(id).getFirst().attachment().id();
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg+1,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  assertThatThrownBy(()->fileLinks.list(id)).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->fileLinks.download(id,fileId)).isInstanceOf(NoSuchElementException.class);
 }
 @Test void sourceAReceiptCannotBeReadOrConfirmedAcrossOrganizations() {
  var r=service.createReceipt(body(List.of(item(reserve(key()),true)),true),key(),false);String id=r.path("id").asText();
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg+1,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  assertThatThrownBy(()->service.get("Receipt",id)).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->service.confirmReceipt(id,new Command(0L,"跨组织记录不得确认"),null,key())).isInstanceOf(NoSuchElementException.class);
 }
'''
pos=s.rfind('}');assert pos>0;s=s[:pos]+extra+s[pos:];f.write_text(s,encoding='utf-8');print('Added four rollback-owned regression cases; no existing assertions or org1 rules changed')
