package com.hospital.mes.security.reauth;

import com.hospital.mes.audit.signature.ExpectedReauthenticationBinding;
import com.hospital.mes.audit.signature.SignatureMeaning;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisReauthenticationTokenStore implements ReauthenticationTokenStore {
    private static final String PREFIX="mes:signature:reauth:";
    private static final DefaultRedisScript<Long> PUT = new DefaultRedisScript<>("""
        local fields={'userId','sessionId','orgId','objectType','objectId','meaning',
          'recordVersion','issuedAt','expiresAt','method'}
        if #KEYS ~= 1 or #ARGV ~= #fields then
          return redis.error_reply('Invalid reauthentication token arguments')
        end
        local keyType=redis.call('TYPE',KEYS[1]).ok
        if keyType ~= 'none' and keyType ~= 'hash' then
          return redis.error_reply('Invalid reauthentication token key type')
        end
        local ok,err=pcall(function()
          for i,field in ipairs(fields) do
            redis.call('HSET',KEYS[1],field,ARGV[i])
            if i == 1 and redis.call('PEXPIRE',KEYS[1],300000) ~= 1 then
              error('Reauthentication token expiry could not be set')
            end
          end
        end)
        if not ok then
          redis.call('DEL',KEYS[1])
          return redis.error_reply(err)
        end
        return 1
        """, Long.class);
    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> CONSUME = new DefaultRedisScript<>("""
        local values=redis.call('HMGET',KEYS[1],'userId','sessionId','orgId','objectType','objectId','meaning','recordVersion','issuedAt','expiresAt','method')
        if not values[1] then return {} end
        redis.call('DEL',KEYS[1])
        return values
        """, List.class);
    private final StringRedisTemplate redis;
    public RedisReauthenticationTokenStore(StringRedisTemplate redis){this.redis=redis;}
    @Override public void put(String token, StoredReauthentication value){
        if(value.method()==null||value.method().isBlank())
            throw new IllegalArgumentException("reauthentication method is required");
        var b=value.binding();
        Long result=redis.execute(PUT,List.of(key(token)),Long.toString(b.userId()),b.sessionId(),
            Long.toString(b.organizationId()),b.objectType(),b.objectId(),b.meaning().name(),
            Long.toString(b.recordVersion()),Long.toString(value.issuedAt().toEpochMilli()),
            Long.toString(value.expiresAt().toEpochMilli()),value.method());
        if(!Long.valueOf(1).equals(result)) throw new IllegalStateException("reauthentication token storage failed");
    }
    @Override @SuppressWarnings("unchecked") public StoredReauthentication consume(String token){
        if(token==null||token.isBlank()) return null;
        List<String> v=(List<String>)(List<?>)redis.execute(CONSUME,List.of(key(token)));
        if(v==null||v.size()!=10) return null;
        try {
            var b=new ExpectedReauthenticationBinding(Long.parseLong(v.get(0)),v.get(1),Long.parseLong(v.get(2)),
                v.get(3),v.get(4),SignatureMeaning.valueOf(v.get(5)),Long.parseLong(v.get(6)));
            return new StoredReauthentication(b,Instant.ofEpochMilli(Long.parseLong(v.get(7))),
                Instant.ofEpochMilli(Long.parseLong(v.get(8))),v.get(9));
        } catch(RuntimeException invalid){return null;}
    }
    private static String key(String token){
        try{return PREFIX+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
