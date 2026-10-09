-- DCP-BASIC-NO-AUDIT-VERSION-001. Preserve historical grants and permission evidence.
UPDATE sys_permission SET enabled=0, version=version+1
 WHERE permission_code='process:package:publish' AND enabled=1;
