package com.hospital.mes.qms.application;
/** Configured qualification values resolve through authoritative master-data records. */
public interface IncomingActorPort {
 void requireUser(long orgId,long userId);
 void requireQualified(long orgId,long userId,String operation);
}
