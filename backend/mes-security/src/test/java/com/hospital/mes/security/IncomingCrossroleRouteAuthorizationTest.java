package com.hospital.mes.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import com.hospital.mes.security.web.SecurityErrorWriter;
import com.hospital.mes.security.web.SessionAuthenticationFilter;
import java.util.stream.Stream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/** Exercises the real ordered filter chain; the probe is not business acceptance. */
@SpringJUnitWebConfig(IncomingCrossroleRouteAuthorizationTest.ProbeConfig.class)
class IncomingCrossroleRouteAuthorizationTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
    static Stream<Arguments> routes(){return Stream.of(
        Arguments.of(HttpMethod.GET,"/api/v1/wms/receipts/485/items/502/inspection-request","wms:receipt:view"),
        Arguments.of(HttpMethod.POST,"/api/v1/wms/receipts/485/items/502/inspection-request","wms:receipt:confirm"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/inspection-requests/220/confirm-and-generate","qms:inspection-request:accept"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/sampling-tasks/330/submit-to-warehouse","qms:sampling:execute"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/sampling-tasks/330/plan","qms:sampling:assign"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/inspection-tasks/440/bind-sample","qms:test:execute"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/samples/550/receive","qms:sample:receive"));}
    @ParameterizedTest @MethodSource("routes")
    void exactExistingPermissionReachesController(HttpMethod method,String path,String permission)throws Exception{
        mvc.perform(request(method,path).with(user("authorized").authorities(new SimpleGrantedAuthority(permission)))).andExpect(status().isOk());
    }
    @ParameterizedTest @MethodSource("routes")
    void anotherPermissionDoesNotGrantAccess(HttpMethod method,String path,String permission)throws Exception{
        mvc.perform(request(method,path).with(user("unauthorized").authorities(new SimpleGrantedAuthority("qms:plan:view")))).andExpect(status().isForbidden());
    }
    @ParameterizedTest @MethodSource("routes")
    void anonymousIsRejected(HttpMethod method,String path,String permission)throws Exception{
        mvc.perform(request(method,path)).andExpect(status().isUnauthorized());
    }
    @ParameterizedTest @MethodSource("routes")
    void extraPathSegmentDoesNotMatch(HttpMethod method,String path,String permission)throws Exception{
        mvc.perform(request(method,path+"/extra").with(user("authorized").authorities(new SimpleGrantedAuthority(permission)))).andExpect(status().isForbidden());
    }
    @ParameterizedTest @MethodSource("warehousePermissionSets")
    void warehouseConfirmationRequiresBothAuthorities(List<String> permissions,int expected)throws Exception{
        var auth=permissions.stream().map(SimpleGrantedAuthority::new).toList();
        mvc.perform(request(HttpMethod.POST,"/api/v1/quality/sampling-tasks/330/confirm-sample-outbound").with(user("warehouse").authorities(auth))).andExpect(status().is(expected));
    }
    static Stream<Arguments> warehousePermissionSets(){return Stream.of(
        Arguments.of(List.of("qms:sampling:warehouse-confirm","wms:inventory:sample-outbound"),200),
        Arguments.of(List.of("qms:sampling:warehouse-confirm"),403),
        Arguments.of(List.of("wms:inventory:sample-outbound"),403));}
    @Configuration @EnableWebMvc @EnableWebSecurity @Import(SecurityConfiguration.class)
    static class ProbeConfig {
        @Bean SecurityErrorWriter errors(){return new SecurityErrorWriter(new ObjectMapper(),()->"route-test");}
        @Bean SessionAuthenticationFilter sessions(SecurityErrorWriter errors){return new SessionAuthenticationFilter(org.mockito.Mockito.mock(AccessTokenCodec.class),org.mockito.Mockito.mock(SessionStore.class),errors);}
        @Bean ProbeController probe(){return new ProbeController();}
    }
    @RestController static class ProbeController {
        @RequestMapping({"/api/v1/wms/receipts/{receiptId}/items/{itemId}/inspection-request","/api/v1/quality/inspection-requests/{id}/confirm-and-generate","/api/v1/quality/sampling-tasks/{id}/submit-to-warehouse","/api/v1/quality/sampling-tasks/{id}/confirm-sample-outbound","/api/v1/quality/sampling-tasks/{id}/plan","/api/v1/quality/inspection-tasks/{id}/bind-sample","/api/v1/quality/samples/{id}/receive"})
        String accept(){return "reached";}
    }
}
