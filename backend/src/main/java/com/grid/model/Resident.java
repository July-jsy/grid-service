package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record Resident(
        Long id,
        @NotBlank(message = "居民姓名不能为空") String name,
        String gender,
        String idCard,
        String phone,
        @NotBlank(message = "所属网格不能为空") String gridName,
        Long gridId,
        String address,
        Long houseId,
        String residentType,
        String remark
) {
}
