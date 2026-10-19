package com.hospital.mes.audit.application;
import java.util.Set;
/** Current command eligibility only; never used to verify historical signatures. */
public interface CurrentSigningAuthority {
    CurrentPlatformContext requireCurrent(CurrentPlatformContext actor, Set<String> permissions);
}
