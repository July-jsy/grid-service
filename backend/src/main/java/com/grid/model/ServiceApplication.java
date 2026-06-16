package com.grid.model;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ServiceApplication(
        Long id,
        String code,
        Long userId,
        Long serviceItemId,
        @NotBlank(message = "申请事项不能为空") String itemName,
        @NotBlank(message = "申请人不能为空") String applicantName,
        String applicantPhone,
        String ownerUsername,
        String content,
        String attachment,
        String status,
        String handleResult,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
