package com.hospital.mes.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.system.application.IamContractService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** DEV-only least-privilege demo role grants through real audited IAM commands. */
@SpringBootTest
@ActiveProfiles("ci")
@Transactional
@Commit
class DemoRoleAssignmentsSeedIT {
    @Autowired IamContractService iam;
    @Autowired JdbcTemplate jdbc;

    record RoleSeed(String code,String title,String login,List<String> permissions) {}
    private static List<String> codes(String... values){return List.of(values);}

    @Test
    void assignSeparateLeastPrivilegeDemoRoles() {
        List<RoleSeed> grants=List.of(
            new RoleSeed("DEMO_MES_MASTER","演示·主数据岗位","demo.master",codes(
                "master:material:view","master:supplier:view","master:qualification:view",
                "master:material:create","master:material:update","master:supplier:create","master:supplier:update")),
            new RoleSeed("DEMO_MES_WAREHOUSE","演示·仓储岗位","demo.warehouse",codes(
                "wms:receipt:view","wms:receipt:create","wms:receipt:confirm",
                "wms:inventory:view","wms:inventory:move",
                "wms:request:view","wms:issue:view","wms:issue:create","wms:issue:confirm","wms:issue:return",
                "wms:finished-inbound:view","wms:finished-inbound:confirm",
                "wms:finished-shipment:view","wms:finished-shipment:create","wms:finished-shipment:confirm",
                "master:material:view","master:supplier:view","ebr:sign")),
            new RoleSeed("DEMO_MES_PRODUCTION","演示·生产岗位","demo.production",codes(
                "production:batch:view","production:batch:create","production:batch:update",
                "production:batch:release","production:batch:start","production:batch:complete",
                "production:order:view","production:order:create","production:order:update",
                "mes:execution:view","mes:operation:view","mes:operation:start","mes:operation:complete","mes:operation:pause",
                "mes:weigh:view","mes:weigh:create","mes:charge:view","mes:charge:create",
                "ebr:form:view","ebr:form:edit","ebr:form:submit","ebr:sign",
                "wms:inventory:view","wms:reservation:view","wms:reservation:create",
                "wms:request:view","wms:request:create","wms:request:submit","wms:issue:view",
                "qms:plan:view","balance:view","trace:view","master:material:view","master:product:view","process:package:view")),
            new RoleSeed("DEMO_MES_SAMPLER","演示·取样岗位","demo.sampler",codes(
                "qms:sampling:view","qms:sampling:execute","qms:sampling:complete",
                "qms:sample:view","qms:sample:create",
                "qms:finished-sampling:view","qms:finished-sampling:create",
                "qms:finished-request:view","production:batch:view","wms:inventory:view",
                "master:qualification:view","master:material:view","ebr:sign")),
            new RoleSeed("DEMO_MES_QC_ANALYST","演示·QC检验岗位","demo.qc.analyst",codes(
                "qms:inspection-request:view","qms:inspection-request:accept",
                "qms:sampling:view","qms:sampling:create","qms:sample:view","qms:sample:receive",
                "qms:test:view","qms:test:execute","qms:test:record",
                "qms:report:view","qms:report:create",
                "qms:finished-request:view","qms:finished-request:accept",
                "qms:finished-sampling:view","qms:finished-report:view","qms:finished-report:generate",
                "qms:specification:view","qms:plan:view","ebr:sign",
                "production:batch:view","master:material:view","master:qualification:view")),
            new RoleSeed("DEMO_MES_QC_REVIEWER","演示·QC复核岗位","demo.qc.reviewer",codes(
                "qms:inspection-request:view","qms:test:view","qms:test:review",
                "qms:report:view","qms:report:review",
                "qms:finished-report:view","qms:finished-report:approve",
                "qms:specification:view","qms:specification:approve",
                "mes:weigh:view","mes:weigh:verify","qms:plan:view","qms:sample:view",
                "ebr:sign","production:batch:view","master:qualification:view")),
            new RoleSeed("DEMO_MES_QA","演示·QA岗位","demo.qa",codes(
                "qa:batch-review","qa:release","qa:material-release:view","qa:material-release:decide",
                "qa:material-inventory:freeze","qa:material-inventory:unfreeze",
                "qms:plan:view","qms:plan:approve","qms:report:view","qms:report:approve",
                "qms:deviation:view","qms:deviation:investigate","qms:deviation:decide","qms:deviation:close",
                "qms:finished-report:view","qms:finished-request:view","qms:finished-request:accept",
                "wms:inventory:view","wms:finished-inbound:view","production:batch:view",
                "ebr:sign","ebr:form:view","balance:view","trace:view",
                "qms:test:view","qms:sample:view","qms:specification:view","qms:finished-sampling:view",
                "master:qualification:view","master:product:view","master:material:view","master:uom:view"))
        );
        // Fail the entire transaction before making changes if any required permission is absent.
        for(RoleSeed seed:grants)for(String code:seed.permissions()){
            Integer found=jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_permission WHERE permission_code=? AND enabled=TRUE",
                Integer.class,code);
            assertThat(found).as("Required registered permission: "+code).isEqualTo(1);
        }
        Long administrator=jdbc.queryForObject(
            "SELECT id FROM sys_user WHERE login_name_normalized='admin'",Long.class);
        Set<String> adminPermissions=new HashSet<>(jdbc.queryForList(
            "SELECT p.permission_code FROM sys_role r JOIN sys_role_permission rp ON rp.role_id=r.id JOIN sys_permission p ON p.id=rp.permission_id WHERE r.role_code='SYSTEM_ADMIN' AND p.enabled=TRUE",
            String.class));
        CurrentPlatformContext actor=new CurrentPlatformContext(
            1,administrator,Set.of("SYSTEM_ADMIN"),adminPermissions,
            "local-demo-rbac","local-demo-rbac");
        int createdRoles=0,assignedUsers=0;
        for(RoleSeed seed:grants){
            List<Long> roleIds=jdbc.queryForList("SELECT id FROM sys_role WHERE role_code=?",Long.class,seed.code());
            IamContractService.IamRoleResponse role;
            if(roleIds.isEmpty()){
                role=iam.createRole(new IamContractService.CreateRoleRequest(
                    seed.code(),seed.title(),"DEV-only qualification and role integration testing"),
                    actor,UUID.randomUUID().toString());
                createdRoles++;
                role=iam.assignRolePermissions(Long.parseLong(role.id()),role.version(),
                    new IamContractService.AssignRolePermissionsRequest(
                        seed.permissions(),"Grant only capabilities of this demo job"),
                    actor,UUID.randomUUID().toString());
            }else{
                role=iam.getRole(roleIds.getFirst());
                if(!new HashSet<>(role.permissionCodes()).equals(new HashSet<>(seed.permissions()))){
                    assertThat(new HashSet<>(seed.permissions()))
                        .as("Never remove existing demo role permissions: "+seed.code())
                        .containsAll(role.permissionCodes());
                    role=iam.assignRolePermissions(Long.parseLong(role.id()),role.version(),
                        new IamContractService.AssignRolePermissionsRequest(
                            seed.permissions(),"Grant additional read-only QA evidence access for DEV review"),
                        actor,UUID.randomUUID().toString());
                }
            }
            List<Long> users=jdbc.queryForList(
                "SELECT id FROM sys_user WHERE login_name_normalized=? AND enabled=TRUE",
                Long.class,seed.login());
            assertThat(users).as("Demo user must exist: "+seed.login()).hasSize(1);
            var user=iam.getUser(users.getFirst());
            if(!user.roleIds().equals(List.of(role.id()))){
                assertThat(user.roleIds()).as("Do not replace non-demo user grants").isEmpty();
                iam.assignUserRoles(users.getFirst(),user.version(),
                    new IamContractService.AssignUserRolesRequest(List.of(role.id()),
                        "DEV-only realistic role-separated integration testing"),
                    actor,UUID.randomUUID().toString());
                assignedUsers++;
            }
            var latest=iam.getUser(users.getFirst());
            assertThat(latest.roleIds()).containsExactly(role.id());
            assertThat(new HashSet<>(iam.getRole(Long.parseLong(role.id())).permissionCodes()))
                .isEqualTo(new HashSet<>(seed.permissions()));
        }
        System.out.println("DEMO_RBAC_ROLES="+grants.size()+",CREATED="+createdRoles+
            ",USER_ASSIGNMENTS="+assignedUsers);
    }
}
