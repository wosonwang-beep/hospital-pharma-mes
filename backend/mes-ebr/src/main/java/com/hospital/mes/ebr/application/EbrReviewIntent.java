package com.hospital.mes.ebr.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.function.Supplier;
@org.springframework.stereotype.Component
public class EbrReviewIntent {
 public record Intent(long org,long actor,SignableObject object,SignatureMeaning meaning){}
 public <T>T execute(CurrentPlatformContext c,SignableObject object,SignatureMeaning meaning,Supplier<T> work){if(!TransactionSynchronizationManager.isActualTransactionActive()||TransactionSynchronizationManager.hasResource(this))throw new IllegalStateException("Review signing requires business transaction");TransactionSynchronizationManager.bindResource(this,new Intent(c.organizationId(),c.actorId(),object,meaning));try{return work.get();}finally{TransactionSynchronizationManager.unbindResource(this);}}
 public Intent current(){return (Intent)TransactionSynchronizationManager.getResource(this);}
}
