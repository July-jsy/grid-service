package com.grid.model;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record Notice(
        Long id,
        @NotBlank(message = "公告标题不能为空") String title,
        @NotBlank(message = "公告分类不能为空") String category,
        @NotBlank(message = "公告内容不能为空") String content,
        LocalDateTime publishedAt
) {
}
