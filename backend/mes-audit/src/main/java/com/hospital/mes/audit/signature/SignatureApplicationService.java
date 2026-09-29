package com.hospital.mes.audit.signature;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SignatureApplicationService {
    private final ReauthenticationPort reauthentication; private final SignatureTransactionService transactions;
    public SignatureApplicationService(ReauthenticationPort reauthentication,SignatureTransactionService transactions){
        this.reauthentication=reauthentication;this.transactions=transactions;
    }
    public SignatureResponseData sign(SignCommand command){
        var expected=new ExpectedReauthenticationBinding(command.context().actorId(),command.context().sessionId(),
            command.context().organizationId(),command.objectType(),command.objectId(),command.meaning(),command.recordVersion());
        ConsumedReauthentication consumed=reauthentication.consume(command.reauthToken(),expected);
        return transactions.signInTransaction(command,consumed);
    }
}
