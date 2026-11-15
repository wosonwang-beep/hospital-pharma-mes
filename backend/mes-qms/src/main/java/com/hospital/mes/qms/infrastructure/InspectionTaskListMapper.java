package com.hospital.mes.qms.infrastructure;

import org.apache.ibatis.annotations.*;
import java.util.*;

/** Bounded cross-domain read projection; every regulated join is scoped by organization. */
@Mapper
public interface InspectionTaskListMapper {
 String NAME="NULLIF(JSON_UNQUOTE(JSON_EXTRACT(l.material_snapshot_json, '$.materialName')), '') COLLATE utf8mb4_unicode_ci";
 String FROM=" FROM qms_inspection_task t LEFT JOIN qms_sample s ON s.id=t.sample_id AND s.org_id=t.org_id"
  +" LEFT JOIN qms_inspection_request r ON r.id=t.inspection_request_id AND r.org_id=t.org_id"
  +" LEFT JOIN md_material_lot l ON l.id=s.material_lot_id AND l.org_id=t.org_id AND l.id=r.material_lot_id"
  +" LEFT JOIN sys_user u ON u.id=t.assigned_to ";
 String FILTER="<where>t.org_id=#{org}"
  +"<if test=\"filters.status != null and filters.status != ''\"> AND t.status=#{filters.status}</if>"
  +"<if test=\"filters.assignedTo != null and filters.assignedTo != ''\"> AND t.assigned_to=#{filters.assignedTo}</if>"
  +"<if test=\"filters.requestType != null and filters.requestType != ''\"> AND r.request_type=#{filters.requestType}</if>"
  +"<if test=\"filters.inspectionRequestId != null and filters.inspectionRequestId != ''\"> AND t.inspection_request_id=#{filters.inspectionRequestId}</if>"
  +"<if test=\"filters.materialLotId != null and filters.materialLotId != ''\"> AND r.material_lot_id=#{filters.materialLotId}</if>"
  +"<if test=\"keyword != null and keyword != ''\"> AND (LOCATE(LOWER(#{keyword}),LOWER(CONCAT_WS(' ',"+NAME+",l.lot_no,s.sample_no,t.inspection_task_no)))>0"
  +" OR EXISTS(SELECT 1 FROM qms_inspection_report_item ri JOIN qms_inspection_item i ON i.id=ri.inspection_item_id AND i.org_id=ri.org_id"
  +" JOIN qms_inspection_report p ON p.id=ri.inspection_report_id AND p.org_id=ri.org_id"
  +" WHERE i.inspection_task_id=t.id AND i.org_id=t.org_id AND p.inspection_request_id=t.inspection_request_id AND LOCATE(LOWER(#{keyword}),LOWER(p.report_no))>0))</if></where>";
 @Select("<script>SELECT COUNT(*)"+FROM+FILTER+"</script>")
 long count(@Param("org") long org,@Param("keyword") String keyword,@Param("filters") Map<String,String> filters);
 @Select("<script>SELECT CAST(t.id AS CHAR) AS id,"+NAME+" AS sampleName,l.lot_no AS lotNo,s.sample_no AS sampleNo,"
  +"t.inspection_task_no AS inspectionTaskNo,r.request_type AS requestType,t.status,CAST(t.assigned_to AS CHAR) AS assignedTo,u.display_name AS assignedName,"
  +"DATE_FORMAT(r.requested_date,'%Y-%m-%d') AS requestedDate,DATE_FORMAT(s.sampled_at,'%Y-%m-%dT%H:%i:%sZ') AS sampledAt"
  +FROM+FILTER+" ORDER BY t.id DESC LIMIT #{size} OFFSET #{offset}</script>")
 List<Map<String,Object>> page(@Param("org") long org,@Param("keyword") String keyword,@Param("filters") Map<String,String> filters,@Param("offset") long offset,@Param("size") int size);
 @Select("SELECT DISTINCT CAST(t.assigned_to AS CHAR) AS value,u.display_name AS label FROM qms_inspection_task t JOIN sys_user u ON u.id=t.assigned_to WHERE t.org_id=#{org} ORDER BY u.display_name,t.assigned_to")
 List<Map<String,Object>> inspectors(@Param("org") long org);
 @Select("<script>SELECT DISTINCT CAST(i.inspection_task_id AS CHAR) AS taskId,CAST(p.id AS CHAR) AS reportId,p.report_no AS reportNo"
  +" FROM qms_inspection_report_item ri JOIN qms_inspection_item i ON i.id=ri.inspection_item_id AND i.org_id=ri.org_id"
  +" JOIN qms_inspection_task t ON t.id=i.inspection_task_id AND t.org_id=i.org_id"
  +" JOIN qms_inspection_report p ON p.id=ri.inspection_report_id AND p.org_id=ri.org_id AND p.inspection_request_id=t.inspection_request_id"
  +" WHERE ri.org_id=#{org} AND t.id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> ORDER BY p.id</script>")
 List<Map<String,Object>> reports(@Param("org") long org,@Param("ids") List<String> ids);
}
