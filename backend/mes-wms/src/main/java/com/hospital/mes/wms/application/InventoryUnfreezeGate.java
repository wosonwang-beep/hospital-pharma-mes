package com.hospital.mes.wms.application;
/** Consumed current QA/date/investigation evidence; ignore only the restriction being removed. */
public interface InventoryUnfreezeGate {void requireUnfreeze(long organizationId,long materialLotId);}
