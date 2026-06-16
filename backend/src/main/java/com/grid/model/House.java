package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record House(
        Long id,
        String houseCode,
        @NotBlank(message = "房屋地址不能为空") String address,
        String building,
        String unit,
        String roomNumber,
        @NotBlank(message = "所属网格不能为空") String gridName,
        Long gridId,
        String houseType,
        String usageStatus,
        String ownerName,
        int residentCount,
        Double longitude,
        Double latitude
) {
}
