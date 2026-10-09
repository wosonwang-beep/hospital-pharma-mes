-- Correct V045 runtime navigation requirement encoding.
-- NavigationTree.required() uses comma-separated permission codes.
UPDATE sys_menu
SET required_permissions='master:product:view,process:package:view',
    version_no=version_no+1
WHERE org_id=1
  AND route_path='/production/prescriptions'
  AND required_permissions='master:product:view;process:package:view';
