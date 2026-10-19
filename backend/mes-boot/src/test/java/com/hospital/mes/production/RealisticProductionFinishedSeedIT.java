package com.hospital.mes.production;

/** Both legacy fixture entry points now execute the same isolated rollback-only product chain.
 * No @Commit, FINISHED material reconstruction or hardcoded database entity identifiers.
 */
class RealisticProductionFinishedSeedIT extends com.hospital.mes.demo.RealisticProductionFinishedSeedIT {}
