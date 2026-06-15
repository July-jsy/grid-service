package com.grid.model;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record Event(
        Long id,
        String code,
        @NotBlank(message = "事件标题不能为空") String title,
        @NotBlank(message = "事件分类不能为空") String category,
        @NotBlank(message = "所属网格不能为空") String gridName,
        String reporter,
        String ownerUsername,
        String description,
        String imageUrl,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
