package com.hospital.mes.system.application;
import com.hospital.mes.system.infrastructure.SysMenuEntity;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class NavigationTreeTest {
    private SysMenuEntity menu(long id,Long parent,String path,String permission,String status) { var m=new SysMenuEntity();m.setId(id);m.setParentId(parent);m.setMenuCode("m"+id);m.setMenuName("Menu "+id);m.setRoutePath(path);m.setPermissionCode(permission);m.setStatus(status);m.setSortNo((int)id);return m; }
    @Test void preservesThreeLevelsAndHidesUnauthorizedLeavesAndEmptyDirectories() {
        var root=menu(1,null,null,null,"ACTIVE");var folder=menu(2,1L,null,null,"ACTIVE");var yes=menu(3,2L,"/users","iam:user:view","ACTIVE");var no=menu(4,2L,"/roles","iam:role:view","ACTIVE");
        var tree=NavigationTree.project(List.of(root,folder,yes,no),Set.of("iam:user:view"));
        assertThat(tree).hasSize(1);assertThat(tree.getFirst().children().getFirst().children()).extracting(NavigationTree.Node::path).containsExactly("/users");
        assertThat(NavigationTree.project(List.of(root,folder,yes,no),Set.of())).isEmpty();
    }
    @Test void disabledParentsAndMissingAdditionalPermissionsCannotLeakMenus() {
        var root=menu(1,null,null,null,"INACTIVE");var child=menu(2,1L,"/users","iam:user:view","ACTIVE");
        assertThat(NavigationTree.project(List.of(root,child),Set.of("iam:user:view"))).isEmpty();
        child.setParentId(null);child.setRequiredPermissions("mes:operation:view");
        assertThat(NavigationTree.project(List.of(child),Set.of("iam:user:view"))).isEmpty();
        assertThat(NavigationTree.project(List.of(child),Set.of("iam:user:view","mes:operation:view"))).hasSize(1);
    }
    @Test void malformedCyclesAndOrphansDoNotBecomeVisibleRoots() {
        var a=menu(1,2L,null,null,"ACTIVE");var b=menu(2,1L,"/users","iam:user:view","ACTIVE");var orphan=menu(3,99L,"/users","iam:user:view","ACTIVE");
        assertThat(NavigationTree.project(List.of(a,b,orphan),Set.of("iam:user:view"))).isEmpty();
    }
    @Test void menuVisibilityIsIndependentEvenWhenTwoPagesShareTheSameViewPermission() {
        var a=menu(1,null,"/units","master:uom:view","ACTIVE");var b=menu(2,null,"/conversions","master:uom:view","ACTIVE");
        assertThat(NavigationTree.project(List.of(a,b),Set.of("master:uom:view"),Set.of("m1"))).extracting(NavigationTree.Node::path).containsExactly("/units");
    }
}
