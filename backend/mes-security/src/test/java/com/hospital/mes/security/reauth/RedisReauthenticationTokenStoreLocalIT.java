package com.hospital.mes.security.reauth;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.audit.signature.ExpectedReauthenticationBinding;
import com.hospital.mes.audit.signature.SignatureMeaning;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="mes.local.real.redis",matches="true")
class RedisReauthenticationTokenStoreLocalIT {
    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private RedisReauthenticationTokenStore store;
    private final Set<String> ownedKeys=new HashSet<>();
    private final Instant now=Instant.ofEpochMilli(System.currentTimeMillis());
    private final ExpectedReauthenticationBinding binding=new ExpectedReauthenticationBinding(
        11568,"local-acceptance-"+UUID.randomUUID(),1,"QcSpecificationVersion","314",SignatureMeaning.APPROVE,1);
    @BeforeEach void connect() throws Exception {
        Path root=Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while(root!=null&&!Files.exists(root.resolve(".env")))root=root.getParent();
        assertThat(root).as("Existing checkout root .env is required").isNotNull();
        Properties env=new Properties();try(var reader=Files.newBufferedReader(root.resolve(".env"),StandardCharsets.UTF_8)){env.load(reader);}
        assertThat(env.getProperty("MES_REDIS_HOST","localhost")).isIn("localhost","127.0.0.1");
        assertThat(env.getProperty("MES_REDIS_PORT","6379")).isEqualTo("6379");
        var config=new RedisStandaloneConfiguration("127.0.0.1",6379);
        String password=env.getProperty("MES_REDIS_PASSWORD","").trim();
        if(!password.isEmpty())config.setPassword(password);
        factory=new LettuceConnectionFactory(config);factory.afterPropertiesSet();factory.start();
        redis=new StringRedisTemplate(factory);redis.afterPropertiesSet();store=new RedisReauthenticationTokenStore(redis);
        assertThat(redis.execute((org.springframework.data.redis.core.RedisCallback<String>)c->c.ping())).isEqualTo("PONG");
    }
    @AfterEach void cleanup() {
        if(redis!=null)for(String key:ownedKeys)redis.delete(key);
        if(factory!=null)factory.destroy();
    }
    private String token() throws Exception {
        String token=UUID.randomUUID().toString();
        String key="mes:signature:reauth:"+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        ownedKeys.add(key);return token;
    }
    private String key(String token) throws Exception {return "mes:signature:reauth:"+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
    private StoredReauthentication value(){return new StoredReauthentication(binding,now,now.plusSeconds(300),"PASSWORD");}
    private ReauthenticationService service(Instant clock){return new ReauthenticationService(null,null,store,Clock.fixed(clock,ZoneOffset.UTC));}
    @Test void actualRedisStoresAllFieldsAndFiveMinuteTtlAndConsumesOnce() throws Exception {
        String token=token();store.put(token,value());
        assertThat(redis.opsForHash().size(key(token))).isEqualTo(10);
        assertThat(redis.getExpire(key(token),TimeUnit.MILLISECONDS)).isBetween(295000L,300000L);
        assertThat(store.consume(token)).isEqualTo(value());
        assertThat(store.consume(token)).isNull();
        assertThat(redis.hasKey(key(token))).isFalse();
    }
    @Test void eachBindingMismatchIsRejectedAndBurnsOnlyItsToken() throws Exception {
        List<ExpectedReauthenticationBinding> wrong=List.of(
            new ExpectedReauthenticationBinding(11569,binding.sessionId(),1,binding.objectType(),binding.objectId(),binding.meaning(),1),
            new ExpectedReauthenticationBinding(11568,"different-session",1,binding.objectType(),binding.objectId(),binding.meaning(),1),
            new ExpectedReauthenticationBinding(11568,binding.sessionId(),2,binding.objectType(),binding.objectId(),binding.meaning(),1),
            new ExpectedReauthenticationBinding(11568,binding.sessionId(),1,"OtherObject",binding.objectId(),binding.meaning(),1),
            new ExpectedReauthenticationBinding(11568,binding.sessionId(),1,binding.objectType(),"315",binding.meaning(),1),
            new ExpectedReauthenticationBinding(11568,binding.sessionId(),1,binding.objectType(),binding.objectId(),SignatureMeaning.VERIFY,1),
            new ExpectedReauthenticationBinding(11568,binding.sessionId(),1,binding.objectType(),binding.objectId(),binding.meaning(),2));
        for(var mismatch:wrong){String token=token();store.put(token,value());
            assertThatThrownBy(()->service(now).consume(token,mismatch)).isInstanceOf(ReauthenticationTokenInvalidException.class);
            assertThatThrownBy(()->service(now).consume(token,binding)).isInstanceOf(ReauthenticationTokenInvalidException.class);
        }
    }
    @Test void expiredRecordAndActualRedisExpirationRejectConsumption() throws Exception {
        String token=token();store.put(token,value());
        assertThatThrownBy(()->service(now.plusSeconds(301)).consume(token,binding)).isInstanceOf(ReauthenticationTokenInvalidException.class);
        String expired=token();store.put(expired,value());redis.expire(key(expired),Duration.ofMillis(1));Thread.sleep(20);
        assertThat(store.consume(expired)).isNull();
    }
    @Test void concurrentConsumersHaveExactlyOneWinner() throws Exception {
        String token=token();store.put(token,value());
        try(var workers=Executors.newFixedThreadPool(8)){
            var start=new CountDownLatch(1);List<Future<StoredReauthentication>> results=new ArrayList<>();
            for(int i=0;i<8;i++)results.add(workers.submit(()->{start.await();return store.consume(token);}));
            start.countDown();int winners=0;for(var result:results)if(result.get(5,TimeUnit.SECONDS)!=null)winners++;
            assertThat(winners).isEqualTo(1);
        }
        assertThat(store.consume(token)).isNull();
    }
    @Test void wrongTypeFailsBeforeMutationAndInvalidArgumentsLeaveNoKey() throws Exception {
        String token=token();redis.opsForValue().set(key(token),"owned-sentinel",Duration.ofSeconds(10));
        assertThatThrownBy(()->store.put(token,value())).isInstanceOf(RuntimeException.class);
        assertThat(redis.opsForValue().get(key(token))).isEqualTo("owned-sentinel");
        assertThat(redis.getExpire(key(token),TimeUnit.MILLISECONDS)).isPositive();
        String invalid=token();
        assertThatThrownBy(()->store.put(invalid,new StoredReauthentication(binding,now,now.plusSeconds(300),null))).isInstanceOf(RuntimeException.class);
        assertThat(redis.hasKey(key(invalid))).isFalse();
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings={" ","\t","\n"," \t\n"})
    void missingOrBlankMethodIsRejectedBeforeAnyRedisWrite(String method) throws Exception {
        String invalid=token();
        assertThatThrownBy(()->store.put(invalid,new StoredReauthentication(binding,now,now.plusSeconds(300),method)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reauthentication method is required");
        assertThat(redis.hasKey(key(invalid))).isFalse();
    }
}
