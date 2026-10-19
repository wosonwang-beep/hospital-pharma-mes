package com.hospital.mes.masterdata.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import com.hospital.mes.common.exception.StateTransitionException;

public final class SupplierRules {
    public static final String PENDING = "待评审";
    public static final String QUALIFIED = "合格";
    public static final String UNQUALIFIED = "不合格";

    private SupplierRules() {}

    public static String qualify(String status, LocalDate validTo, boolean enabled, Instant at) {
        if (!enabled || !PENDING.equals(status)) {
            throw new StateTransitionException("INVALID_STATE", "只有已启用且待评审的供应商可以评审合格");
        }
        if (expired(validTo, at)) {
            throw new MasterGateException("SUPPLIER_NOT_APPROVED", "RENEW_QUALIFICATION");
        }
        return QUALIFIED;
    }

    public static String reject(String status, boolean enabled) {
        if (!enabled || !(PENDING.equals(status) || QUALIFIED.equals(status))) {
            throw new StateTransitionException("INVALID_STATE", "当前资格状态不能评审为不合格");
        }
        return UNQUALIFIED;
    }

    public static String review(String status, boolean enabled) {
        if (!enabled || !(QUALIFIED.equals(status) || UNQUALIFIED.equals(status))) {
            throw new StateTransitionException("INVALID_STATE", "只有合格或不合格的供应商可以重新评审");
        }
        return PENDING;
    }

    public static boolean expired(LocalDate to, Instant at) {
        return to != null && at.atZone(ZoneOffset.UTC).toLocalDate().isAfter(to);
    }

    public static void usable(String status, boolean enabled, LocalDate supplierTo,
                              boolean approved, LocalDate relationTo, Instant at) {
        if (!enabled || !QUALIFIED.equals(status) || expired(supplierTo, at)
                || !approved || expired(relationTo, at)) {
            throw new MasterGateException("SUPPLIER_NOT_APPROVED", "SELECT_VALID_APPROVED_SUPPLIER");
        }
    }
}
