package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.UserView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/login")
    public Result<UserView> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        if (!"admin".equals(request.username()) || !"admin123".equals(request.password())) {
            return Result.fail("用户名或密码错误");
        }
        var user = new UserView("admin", "系统管理员", "管理员");
        session.setAttribute("user", user);
        return Result.ok(user);
    }

    @GetMapping("/me")
    public Result<Object> me(HttpServletRequest request) {
        return Result.ok(request.getSession().getAttribute("user"));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }
}
