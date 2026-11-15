-- User-approved global policy: no database triggers and no GxP audit event feature.
-- This migration intentionally contains DROP TRIGGER only; it creates no triggers.
DROP TRIGGER IF EXISTS bd_ebr_book_pdf;
DROP TRIGGER IF EXISTS bd_ebr_book_template;
DROP TRIGGER IF EXISTS bd_ebr_pdf_manifest;
DROP TRIGGER IF EXISTS bd_mes_balance_investigation;
DROP TRIGGER IF EXISTS bd_mes_balance_result;
DROP TRIGGER IF EXISTS bd_mes_balance_rule;
DROP TRIGGER IF EXISTS bd_mes_clearance_record;
DROP TRIGGER IF EXISTS bd_mes_clearance_review;
DROP TRIGGER IF EXISTS bd_mes_print_artifact;
DROP TRIGGER IF EXISTS bd_mes_print_artifact_source_evidence;
DROP TRIGGER IF EXISTS bd_mes_print_batch;
DROP TRIGGER IF EXISTS bd_mes_print_batch_source_evidence;
DROP TRIGGER IF EXISTS bd_mes_print_binding;
DROP TRIGGER IF EXISTS bd_mes_print_data_source;
DROP TRIGGER IF EXISTS bd_mes_print_data_source_revision;
DROP TRIGGER IF EXISTS bd_mes_print_source_validation;
DROP TRIGGER IF EXISTS bd_mes_print_template_version;
DROP TRIGGER IF EXISTS bd_qms_capa;
DROP TRIGGER IF EXISTS bd_qms_capa_review;
DROP TRIGGER IF EXISTS bd_qms_deviation;
DROP TRIGGER IF EXISTS bd_qms_inspection_item;
DROP TRIGGER IF EXISTS bd_qms_inspection_report;
DROP TRIGGER IF EXISTS bd_qms_inspection_report_item;
DROP TRIGGER IF EXISTS bd_qms_inspection_request;
DROP TRIGGER IF EXISTS bd_qms_inspection_request_item;
DROP TRIGGER IF EXISTS bd_qms_inspection_task;
DROP TRIGGER IF EXISTS bd_qms_ipc_instance;
DROP TRIGGER IF EXISTS bd_qms_ipc_result_revision;
DROP TRIGGER IF EXISTS bd_qms_ipc_review;
DROP TRIGGER IF EXISTS bd_qms_production_plan;
DROP TRIGGER IF EXISTS bd_qms_production_test_instance;
DROP TRIGGER IF EXISTS bd_qms_production_test_review;
DROP TRIGGER IF EXISTS bd_qms_release_decision;
DROP TRIGGER IF EXISTS bd_qms_sample;
DROP TRIGGER IF EXISTS bd_qms_sampling_detail;
DROP TRIGGER IF EXISTS bd_qms_sampling_task;
DROP TRIGGER IF EXISTS bd_qms_test_execution;
DROP TRIGGER IF EXISTS bd_qms_test_result_revision;
DROP TRIGGER IF EXISTS bd_wms_material_lot_inventory_decision;
DROP TRIGGER IF EXISTS bi_ebr_book_pdf_scope;
DROP TRIGGER IF EXISTS bi_shared_ebr_scope;
DROP TRIGGER IF EXISTS bu_ebr_book_pdf;
DROP TRIGGER IF EXISTS bu_ebr_book_template;
DROP TRIGGER IF EXISTS bu_ebr_pdf_manifest;
DROP TRIGGER IF EXISTS bu_mes_balance_result;
DROP TRIGGER IF EXISTS bu_mes_balance_rule;
DROP TRIGGER IF EXISTS bu_mes_clearance_record;
DROP TRIGGER IF EXISTS bu_mes_clearance_review;
DROP TRIGGER IF EXISTS bu_mes_print_artifact;
DROP TRIGGER IF EXISTS bu_mes_print_artifact_source_evidence;
DROP TRIGGER IF EXISTS bu_mes_print_batch;
DROP TRIGGER IF EXISTS bu_mes_print_batch_source_evidence;
DROP TRIGGER IF EXISTS bu_mes_print_binding;
DROP TRIGGER IF EXISTS bu_mes_print_data_source;
DROP TRIGGER IF EXISTS bu_mes_print_data_source_revision;
DROP TRIGGER IF EXISTS bu_mes_print_template_version;
DROP TRIGGER IF EXISTS bu_print_template_source_reference;
DROP TRIGGER IF EXISTS bu_print_template_type;
DROP TRIGGER IF EXISTS bu_qms_capa_review;
DROP TRIGGER IF EXISTS bu_qms_deviation;
DROP TRIGGER IF EXISTS bu_qms_inspection_item;
DROP TRIGGER IF EXISTS bu_qms_inspection_report;
DROP TRIGGER IF EXISTS bu_qms_inspection_report_item;
DROP TRIGGER IF EXISTS bu_qms_inspection_request_item;
DROP TRIGGER IF EXISTS bu_qms_inspection_task;
DROP TRIGGER IF EXISTS bu_qms_ipc_result_revision;
DROP TRIGGER IF EXISTS bu_qms_ipc_review;
DROP TRIGGER IF EXISTS bu_qms_production_test_review;
DROP TRIGGER IF EXISTS bu_qms_release_decision;
DROP TRIGGER IF EXISTS bu_qms_sample;
DROP TRIGGER IF EXISTS bu_qms_sampling_detail;
DROP TRIGGER IF EXISTS bu_qms_sampling_task;
DROP TRIGGER IF EXISTS bu_qms_test_execution;
DROP TRIGGER IF EXISTS bu_qms_test_result_revision;
DROP TRIGGER IF EXISTS bu_shared_ebr_scope;
DROP TRIGGER IF EXISTS bu_wms_material_lot_inventory_decision;
DROP TRIGGER IF EXISTS ebr_form_insert;
DROP TRIGGER IF EXISTS ebr_form_no_delete;
DROP TRIGGER IF EXISTS ebr_form_update;
DROP TRIGGER IF EXISTS ebr_review_insert;
DROP TRIGGER IF EXISTS ebr_review_no_delete;
DROP TRIGGER IF EXISTS ebr_review_update;
DROP TRIGGER IF EXISTS ebr_rule_insert;
DROP TRIGGER IF EXISTS ebr_rule_no_delete;
DROP TRIGGER IF EXISTS ebr_rule_no_update;
DROP TRIGGER IF EXISTS ebr_snapshot_insert;
DROP TRIGGER IF EXISTS ebr_snapshot_no_delete;
DROP TRIGGER IF EXISTS ebr_snapshot_no_update;
DROP TRIGGER IF EXISTS ebr_value_insert;
DROP TRIGGER IF EXISTS ebr_value_no_delete;
DROP TRIGGER IF EXISTS ebr_value_no_update;
DROP TRIGGER IF EXISTS freeze_finished_receipt_confirmation;
DROP TRIGGER IF EXISTS freeze_finished_report_approval;
DROP TRIGGER IF EXISTS gxp_attachment_no_delete;
DROP TRIGGER IF EXISTS gxp_attachment_no_update;
DROP TRIGGER IF EXISTS md_conversion_generic_insert;
DROP TRIGGER IF EXISTS md_conversion_generic_update;
DROP TRIGGER IF EXISTS no_qms_finished_inspection_report_delete;
DROP TRIGGER IF EXISTS no_qms_finished_inspection_request_delete;
DROP TRIGGER IF EXISTS no_qms_finished_report_review_delete;
DROP TRIGGER IF EXISTS no_qms_finished_report_review_update;
DROP TRIGGER IF EXISTS no_qms_finished_sampling_record_delete;
DROP TRIGGER IF EXISTS no_qms_finished_sampling_record_update;
DROP TRIGGER IF EXISTS no_wms_finished_inbound_request_delete;
DROP TRIGGER IF EXISTS no_wms_finished_shipment_delete;
DROP TRIGGER IF EXISTS prd_snapshot_no_delete;
DROP TRIGGER IF EXISTS prd_snapshot_no_update;
DROP TRIGGER IF EXISTS preserve_finished_shipment;
DROP TRIGGER IF EXISTS preserve_qms_finished_inspection_report;
DROP TRIGGER IF EXISTS preserve_qms_finished_inspection_request;
DROP TRIGGER IF EXISTS preserve_wms_finished_inbound_request;
DROP TRIGGER IF EXISTS receipt_attachment_no_delete;
DROP TRIGGER IF EXISTS receipt_attachment_no_update;
DROP TRIGGER IF EXISTS trg_gxp_audit_event_no_delete;
DROP TRIGGER IF EXISTS trg_gxp_audit_event_no_update;
DROP TRIGGER IF EXISTS trg_issue_return_delete;
DROP TRIGGER IF EXISTS trg_issue_return_update;
DROP TRIGGER IF EXISTS trg_mes_genealogy_delete;
DROP TRIGGER IF EXISTS trg_mes_genealogy_update;
DROP TRIGGER IF EXISTS trg_mes_parameter_value_delete;
DROP TRIGGER IF EXISTS trg_mes_parameter_value_update;
DROP TRIGGER IF EXISTS trg_mes_quantity_event_delete;
DROP TRIGGER IF EXISTS trg_mes_quantity_event_update;
DROP TRIGGER IF EXISTS trg_wms_ledger_no_delete;
DROP TRIGGER IF EXISTS trg_wms_ledger_no_update;

-- Preserve the role-policy identifiers already bound to existing EBR signatures.
UPDATE gxp_signature s
JOIN (
    SELECT CAST(object_id AS UNSIGNED) AS signature_id, JSON_ARRAYAGG(reason) AS role_policies
    FROM gxp_audit_event
    WHERE action = 'EBR_FORM_SIGNATURE_ROLE' AND object_type = 'EBR_SIGNATURE_POLICY'
    GROUP BY object_id
) p ON p.signature_id = s.id
SET s.auth_context_json = JSON_SET(
    COALESCE(NULLIF(s.auth_context_json, ''), '{}'),
    '$.providerContext',
    JSON_OBJECT('rolePolicies', JSON_EXTRACT(p.role_policies, '$'))
);
-- Remove menu/permission references before deleting the menu and permission rows.
DELETE FROM sys_menu_permission WHERE menu_id=(SELECT id FROM sys_menu WHERE route_path='/audit');
DELETE FROM sys_menu_permission WHERE permission_id=(SELECT id FROM sys_permission WHERE permission_code='audit:view');
DELETE FROM sys_role_permission WHERE permission_id=(SELECT id FROM sys_permission WHERE permission_code='audit:view');
UPDATE sys_security_event SET target_permission_id=NULL WHERE target_permission_id=(SELECT id FROM sys_permission WHERE permission_code='audit:view');
UPDATE sys_permission SET parent_permission_id=NULL WHERE parent_permission_id=(SELECT id FROM sys_permission WHERE permission_code='audit:view');
UPDATE sys_menu SET parent_id=NULL WHERE parent_id=(SELECT id FROM sys_menu WHERE route_path='/audit');
DELETE FROM sys_role_menu WHERE menu_id=(SELECT id FROM sys_menu WHERE route_path='/audit');
DELETE FROM sys_menu WHERE route_path='/audit';
DELETE FROM sys_permission WHERE permission_code='audit:view';

-- Remove GxP audit trail storage.
DROP TABLE IF EXISTS gxp_audit_event;
