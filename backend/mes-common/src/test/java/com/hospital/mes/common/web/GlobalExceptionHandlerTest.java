package com.hospital.mes.common.web;
import static org.junit.jupiter.api.Assertions.*;
import com.hospital.mes.common.exception.ResourceConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler=new GlobalExceptionHandler(()->"trace-1");
    @Test void mapsConflictToFrozenApiError() { var response=handler.mes(new ResourceConflictException("EQUIPMENT_OCCUPIED","occupied")); assertEquals(HttpStatus.CONFLICT,response.getStatusCode()); assertEquals("trace-1",response.getBody().requestId()); assertEquals("EQUIPMENT_OCCUPIED",response.getBody().code()); assertEquals(java.util.List.of(),response.getBody().fieldErrors()); assertEquals(java.util.List.of(),response.getBody().allowedActions()); }
    @Test void hidesUnexpectedMessage() { var response=handler.unexpected(new RuntimeException("secret=password")); assertEquals("Internal server error",response.getBody().message()); assertFalse(response.getBody().message().contains("password")); }
}
