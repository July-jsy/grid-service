package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record Resident(
        Long id,
        @NotBlank(message = "居民姓名不能为空") String name,
        String gender,
        String phone,
        @NotBlank(message = "所属网格不能为空") String gridName,
        String address,
        String type
) {
}
