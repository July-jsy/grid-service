package com.grid.model;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ServiceApplication(
        Long id,
        String code,
        @NotBlank(message = "申请事项不能为空") String itemName,
        @NotBlank(message = "申请人不能为空") String applicant,
        String ownerUsername,
        String phone,
        String materialsNote,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
