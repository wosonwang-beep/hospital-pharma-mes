package com.hospital.mes.qc.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.PermissionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.function.Supplier;
/** A transient transaction-scoped command adapter; never a persistent signing source of truth. */
@Component
public class QcSigningIntent {
 public record Intent(long org,long actor,long versionId,String action,String reason){}
 public <T>T execute(CurrentPlatformContext c,long id,String action,String reason,Supplier<T> work){if(!TransactionSynchronizationManager.isActualTransactionActive()||TransactionSynchronizationManager.hasResource(this))throw new IllegalStateException("QC signing requires its business transaction");TransactionSynchronizationManager.bindResource(this,new Intent(c.organizationId(),c.actorId(),id,action,reason));try{return work.get();}finally{TransactionSynchronizationManager.unbindResource(this);}}
 public Intent current(){return (Intent)TransactionSynchronizationManager.getResource(this);}
 public Intent require(CurrentPlatformContext c,long id,String action){Intent i=current();if(i==null||!TransactionSynchronizationManager.isActualTransactionActive()||i.org()!=c.organizationId()||i.actor()!=c.actorId()||i.versionId()!=id||!i.action().equals(action))throw new PermissionException("PERMISSION_DENIED","QC signature requires the authorized business command");return i;}
}
