package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record House(
        Long id,
        @NotBlank(message = "房屋地址不能为空") String address,
        @NotBlank(message = "所属网格不能为空") String gridName,
        String propertyType,
        String rentalStatus,
        String ownerName,
        int residentCount
) {
}
