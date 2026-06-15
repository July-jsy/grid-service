package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record Grid(
        Long id,
        @NotBlank(message = "网格编号不能为空") String code,
        @NotBlank(message = "网格名称不能为空") String name,
        @NotBlank(message = "所属社区不能为空") String community,
        String description,
        String staffName,
        String staffPhone,
        int residentCount
) {
}
