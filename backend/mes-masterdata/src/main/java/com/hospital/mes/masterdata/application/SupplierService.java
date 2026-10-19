package com.hospital.mes.masterdata.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.common.exception.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class SupplierService {
    private final SupplierStore store;
    private final MasterMutation mutations;

    public SupplierService(SupplierStore store, MasterMutation mutations) {
        this.store = store;
        this.mutations = mutations;
    }

    public ScopedStore.PageData<JsonNode> list(int page, int size, String keyword, Map<String,String> filters) {
        var c = mutations.context("master:supplier:view");
        var p = store.list(c.organizationId(), page, size, keyword, filters);
        return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(), p.total(), page, size);
    }

    public JsonNode get(String id) {
        var c = mutations.context("master:supplier:view");
        return mutations.view(store.get(c.organizationId(), MasterMutation.id(id)));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public JsonNode create(SupplierCommands.Create r, String key) {
        var c = mutations.context("master:supplier:create");
        return mutations.execute(c, "Supplier:CREATE", key, r, () -> {
            var e = new SupplierEntity();
            e.setSupplierCode(MasterRules.text(r.supplierCode(), 64));
            e.setSupplierName(MasterRules.text(r.supplierName(), 200));
            e.setValidTo(r.validTo());
            e.setQualificationStatus(SupplierRules.PENDING);
            e.setEnabled(true);
            store.insert(e, c.organizationId(), c.actorId());
            return e;
        });
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public JsonNode command(String id, SupplierCommands.Update r, String header, String key) {
        var c = mutations.context("master:supplier:update");
        var action = MasterRules.text(r.action(), 40);
        var reviewAction = List.of("QUALIFY", "REJECT", "REVIEW").contains(action);
        if (reviewAction && (r.reason() == null || r.reason().isBlank())) {
            throw new IllegalArgumentException("资格评审必须填写评审依据");
        }
        var reason = MasterRules.text(r.reason() == null || r.reason().isBlank()
                ? "基础资料维护（系统记录）" : r.reason(), 1000);
        return mutations.execute(c, "Supplier:" + id, key,
                Map.of("id", id, "version", 0L, "action", action, "reason", reason, "body", r),
                () -> {
                    var e = store.lock(c.organizationId(), MasterMutation.id(id));
                    List<String> fields;
                    switch (action) {
                        case "UPDATE" -> {
                            if (!Boolean.TRUE.equals(e.getEnabled())) throw invalid("停用供应商不能修改资料");
                            e.setSupplierName(MasterRules.text(r.supplierName(), 200));
                            e.setValidTo(r.validTo());
                            fields = List.of("supplierName", "validTo");
                        }
                        case "QUALIFY" -> {
                            e.setQualificationStatus(SupplierRules.qualify(e.getQualificationStatus(),
                                    e.getValidTo(), Boolean.TRUE.equals(e.getEnabled()), Instant.now()));
                            fields = List.of("qualificationStatus");
                        }
                        case "REJECT" -> {
                            e.setQualificationStatus(SupplierRules.reject(e.getQualificationStatus(),
                                    Boolean.TRUE.equals(e.getEnabled())));
                            fields = List.of("qualificationStatus");
                        }
                        case "REVIEW" -> {
                            e.setQualificationStatus(SupplierRules.review(e.getQualificationStatus(),
                                    Boolean.TRUE.equals(e.getEnabled())));
                            fields = List.of("qualificationStatus");
                        }
                        case "DISABLE" -> {
                            if (!Boolean.TRUE.equals(e.getEnabled())) throw invalid("供应商已停用");
                            e.setEnabled(false);
                            fields = List.of("enabled");
                        }
                        case "ENABLE" -> {
                            if (Boolean.TRUE.equals(e.getEnabled())) throw invalid("供应商已启用");
                            e.setEnabled(true);
                            fields = List.of("enabled");
                        }
                        default -> throw new IllegalArgumentException("未知供应商操作");
                    }
                    store.update(e, 0L, c.actorId(), fields);
                    return e;
                });
    }

    private static StateTransitionException invalid(String reason) {
        return new StateTransitionException("INVALID_STATE", reason);
    }
}
