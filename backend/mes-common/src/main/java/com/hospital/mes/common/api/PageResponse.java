package com.hospital.mes.common.api;
import java.util.List;
public record PageResponse<T>(long page, long size, long total, List<T> records) {
    public PageResponse { records = List.copyOf(records); }
}
