package com.grid.model;

import jakarta.validation.constraints.NotBlank;

public record SystemUser(
        Long id,
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "姓名不能为空") String displayName,
        String password,
        String role,
        String phone,
        String status
) {
}
