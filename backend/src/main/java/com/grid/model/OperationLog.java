package com.grid.model;

import java.time.LocalDateTime;

public record OperationLog(
        Long id,
        Long userId,
        String username,
        String operation,
        String module,
        LocalDateTime createTime
) {
}
