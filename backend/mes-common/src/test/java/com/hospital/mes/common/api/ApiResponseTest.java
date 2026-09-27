package com.hospital.mes.common.api;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
class ApiResponseTest {
    @Test void successUsesStableEnvelope() { assertEquals(new ApiResponse<>("OK","Success",42,"t1"), ApiResponse.success(42,"t1")); }
    @Test void pageCopiesRecords() { var values=new ArrayList<>(java.util.List.of("a")); var page=new PageResponse<>(1,20,1,values); values.add("b"); assertEquals(java.util.List.of("a"),page.records()); }
}
