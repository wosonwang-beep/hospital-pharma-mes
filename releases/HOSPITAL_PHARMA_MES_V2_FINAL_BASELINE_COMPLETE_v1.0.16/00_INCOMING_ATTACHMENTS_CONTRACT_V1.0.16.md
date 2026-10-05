# Approved incoming attachment contract

2026-10-03. Implements the explicit approval of DCP-INCOMING-QUALITY-GAPS-001, DG-06. Included in the reviewed cumulative v1.0.16 before product implementation. Existing eBR attachment facts are not changed.

## Database and ownership

Platform `gxp_attachment` is the single immutable attachment fact. Fields: id BIGINT PK AUTO_INCREMENT; org_id BIGINT NN; uploaded_by BIGINT NN FK sys_user.id; uploaded_at DATETIME(3) NN; file_name VARCHAR(255) NN; media_type VARCHAR(127) NN; byte_length BIGINT NN CHECK 1..10485760; sha256 CHAR(64) NN; content MEDIUMBLOB NN; record_version BIGINT NN DEFAULT 1; retention_status VARCHAR(20) NN CHECK RETAINED. Index(org_id,id). Actual length and hash validated server-side; original name is metadata, never a filesystem path. Blob storage makes upload/audit/idempotency atomic in the native database; no external storage service is required. No UPDATE/DELETE of committed attachment facts.

WMS `wms_receipt_attachment` stores append-only association: id BIGINT PK AUTO_INCREMENT; org_id BIGINT NN; receipt_id BIGINT NN FK wms_material_receipt.id; attachment_id BIGINT NN FK gxp_attachment.id; purpose VARCHAR(20) NN CHECK COA/DELIVERY_DOCUMENT/OTHER; reason VARCHAR(1000) NN; linked_by BIGINT NN FK sys_user.id; linked_at DATETIME(3) NN; record_version BIGINT NN DEFAULT 1. UNIQUE(org_id,receipt_id,attachment_id). Service verifies organization for both targets. No duplicate binary or independent second copy of receipt facts.

Both tables reject UPDATE and DELETE through append-only migration triggers, consistent with retained GMP evidence. There is no unbind/delete route. An incorrect association is preserved and addressed through the existing controlled investigation process; no new correction workflow is invented here.

## API and permissions

IDs are positive decimal strings; timestamps UTC; decimals and dates follow existing conventions. Existing ApiResponse envelope is API-only. Upload/list/link success 200; not found 404, forbidden 403, optimistic/idempotency conflict 409, business gate 422. Generic HTTP payload-size rejection may be 413.

- POST `/attachments`: multipart file, `Idempotency-Key`, permission `attachment:upload`. Metadata and content are immutable. Server derives media type fallback application/octet-stream, length and SHA256, rejects empty/over-limit file and filename longer than 255, and removes control/path characters from download filename. Response AttachmentMetadata: id, fileName, mediaType, byteLength, sha256, uploadedBy, uploadedAt, recordVersion, retentionStatus. Idempotency digest includes exact content hash and submitted metadata, never credentials.
- GET `/attachments/{id}`: `attachment:view`, same organization, returns metadata only.
- GET `/attachments/{id}/content`: same guard, streams exact stored bytes with Content-Disposition attachment and X-Content-Type-Options nosniff; never executes uploaded content.
- GET `/wms/receipts/{id}/attachments`: `wms:receipt:view`, same organization, returns associations with AttachmentMetadata.
- POST `/wms/receipts/{id}/attachments`: `wms:receipt:update` plus `attachment:upload`, Idempotency-Key and quoted If-Match matching versionNo, `{versionNo,attachmentId,purpose,reason}`. Lock receipt, verify expected version and source attachment organization. The linker must be the uploader or separately hold attachment:view; same organization alone does not grant access to another user's upload. DRAFT or APPROVED receipt may receive supplementary evidence; existing receipt fields and receipt version are not overwritten. A link is a new immutable association with its own audit fact, not a receipt edit. Receipt version is checked to reject references from a stale receipt screen; no signature is added at this point.
- GET `/wms/receipts/{id}/attachments/{attachmentId}/content`: same organization, `wms:receipt:view`, verify the actual association; avoids granting broad attachment browsing to receipt readers.

Upload and association emit ATTACHMENT_UPLOADED and RECEIPT_ATTACHMENT_LINKED audit actions within their database transaction. Audit includes metadata digests and link reason; excludes binary payload. Read/download is permission checked. Existing enterprise read-access audit policy applies; no artificial electronic signature is created for file upload.

## UI and frozen contract integration

Use the existing receipt View/Create/Edit visual system and inline labels. Receipt detail gains a 随货资料 table: 文件名、用途、大小、上传人、上传时间、SHA256、下载. With required permissions, a dedicated attachment section lets the user select a file, purpose and reason, upload, then link using the latest receipt version. Display and retry a successful upload if linking fails; never automatically re-upload duplicate bytes after a link conflict. No new route or page redesign. 409 preserves selection/reason and asks reload; 422 retains input.

Required tests: exact-byte/hash roundtrip; cross-org metadata/content/link denied; empty/oversized input rejected; same-key replay/conflict; receipt link stale version and duplicate association; approved receipt unchanged while append link succeeds; audit failure rollback; append-only DB protection; UI inline labels and preserved input. RTM WMS-001 and incoming receipt completeness references are extended. Missing COA is shown as missing evidence, never fabricated or inferred from packaging checks; no unconditional requirement for a COA is imposed beyond the selected approved quality standard/policy.


## Approved functional closure delta — v1.0.16

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.16.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.16.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
