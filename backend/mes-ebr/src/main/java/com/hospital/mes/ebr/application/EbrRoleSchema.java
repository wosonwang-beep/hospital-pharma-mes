package com.hospital.mes.ebr.application;

import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.system.application.SystemReferenceQuery;
import java.util.*;

/** Template definition schema only; independent from the generic signature envelope schema. */
final class EbrRoleSchema {
    private EbrRoleSchema(){}
    static Definition prepare(Definition input,SystemReferenceQuery roles){
        if(input==null||!"2.0".equals(input.schemaVersion()))throw new IllegalArgumentException("Explicit schema2 definition required for draft save/conversion");
        var shape=com.hospital.mes.ebr.domain.EbrDefinitionRules.lint(input,false);if(!shape.isEmpty())throw new IllegalArgumentException(shape.getFirst().path()+": "+shape.getFirst().message());
        var signatures=input.signatureRules().stream().map(r->{inputOnly(r.requiredRole(),r.roleCodeSnapshot(),r.roleNameSnapshot());var role=roles.requireRoleId(r.requiredRoleId());return new SignatureRule(r.objectScope(),r.objectCode(),r.meaning(),null,r.reauthRequired(),r.sequenceNo(),r.invalidateOnChange(),role.id(),role.code(),role.name());}).toList();
        var reviews=input.reviewRules().stream().map(r->{inputOnly(r.requiredRole(),r.roleCodeSnapshot(),r.roleNameSnapshot());var role=roles.requireRoleId(r.requiredRoleId());return new ReviewRule(r.objectScope(),r.objectCode(),r.reviewType(),null,r.independentUserRequired(),r.sequenceNo(),role.id(),role.code(),role.name());}).toList();
        return new Definition(input.sections(),input.forms(),input.rules(),signatures,reviews,"2.0");
    }
    private static void inputOnly(String oldCode,String code,String name){if(oldCode!=null||code!=null||name!=null)throw new IllegalArgumentException("Role snapshots are server generated; submit only requiredRoleId");}
    static Definition copyInput(Definition source,SystemReferenceQuery roles){
        var signatures=source.signatureRules().stream().map(r->{String id="2.0".equals(source.schemaVersion())?roles.requireRoleId(r.requiredRoleId()).id():roles.enabledRoleByCode(r.requiredRole()).id();return new SignatureRule(r.objectScope(),r.objectCode(),r.meaning(),null,r.reauthRequired(),r.sequenceNo(),r.invalidateOnChange(),id,null,null);}).toList();
        var reviews=source.reviewRules().stream().map(r->{String id="2.0".equals(source.schemaVersion())?roles.requireRoleId(r.requiredRoleId()).id():roles.enabledRoleByCode(r.requiredRole()).id();return new ReviewRule(r.objectScope(),r.objectCode(),r.reviewType(),null,r.independentUserRequired(),r.sequenceNo(),id,null,null);}).toList();
        return new Definition(source.sections(),source.forms(),source.rules(),signatures,reviews,"2.0");
    }
    static void validate(String schema,String id,String legacyCode,String code,String name,boolean frozen,SystemReferenceQuery roles){
        if(!"2.0".equals(schema)){if(!frozen)roles.requireRole(legacyCode);return;}
        if(id==null||!id.matches("[1-9][0-9]*")||Long.parseLong(id)<=0||legacyCode!=null||code==null||code.isBlank()||name==null||name.isBlank())throw new IllegalArgumentException("Complete schema2 role ID and server snapshots required");
        if(!frozen){var current=roles.requireRoleId(id);if(!current.code().equals(code)||!current.name().equals(name))throw new IllegalArgumentException("Role display changed; save draft to refresh server snapshots before transition");}
    }
}
