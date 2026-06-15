package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record ServiceItem(
        Long id,
        @NotBlank(message = "事项名称不能为空") String name,
        @NotBlank(message = "服务分类不能为空") String category,
        String department,
        int deadlineDays,
        String materials,
        String status
) {
}
