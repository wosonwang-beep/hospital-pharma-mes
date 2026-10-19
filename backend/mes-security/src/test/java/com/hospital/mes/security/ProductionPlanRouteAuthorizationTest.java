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
@SpringJUnitWebConfig(ProductionPlanRouteAuthorizationTest.ProbeConfig.class)
class ProductionPlanRouteAuthorizationTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
    static Stream<Arguments> routes(){return Stream.of(
        Arguments.of(HttpMethod.POST,"/api/v1/quality/production-plans","qms:plan:create"),
        Arguments.of(HttpMethod.PUT,"/api/v1/quality/production-plans/314","qms:plan:update"),
        Arguments.of(HttpMethod.POST,"/api/v1/quality/production-plans/314/approve","qms:plan:approve"),
        Arguments.of(HttpMethod.GET,"/api/v1/finished-inventory","wms:inventory:view"),
        Arguments.of(HttpMethod.GET,"/api/v1/ebr/role-options","ebr:template:view"));}
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
    @Configuration @EnableWebMvc @EnableWebSecurity @Import(SecurityConfiguration.class)
    static class ProbeConfig {
        @Bean SecurityErrorWriter errors(){return new SecurityErrorWriter(new ObjectMapper(),()->"route-test");}
        @Bean SessionAuthenticationFilter sessions(SecurityErrorWriter errors){return new SessionAuthenticationFilter(org.mockito.Mockito.mock(AccessTokenCodec.class),org.mockito.Mockito.mock(SessionStore.class),errors);}
        @Bean ProbeController probe(){return new ProbeController();}
    }
    @RestController static class ProbeController {
        @RequestMapping({"/api/v1/quality/production-plans","/api/v1/quality/production-plans/{id}","/api/v1/quality/production-plans/{id}/approve","/api/v1/finished-inventory","/api/v1/ebr/role-options"})
        String accept(){return "reached";}
    }
}
