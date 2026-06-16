package com.grid.controller;

import com.grid.common.Result;
import com.grid.config.JwtUtil;
import com.grid.model.SystemUser;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final DataStore store;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(DataStore store, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "姓名不能为空") String displayName,
            @NotBlank(message = "密码不能为空") String password,
            String phone
    ) {}
    public record PasswordChangeRequest(
            @NotBlank(message = "旧密码不能为空") String oldPassword,
            @NotBlank(message = "新密码不能为空") String newPassword
    ) {}
    public record ProfileUpdateRequest(String displayName, String phone) {}

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        var account = store.users().stream()
                .filter(item -> item.username().equals(request.username()) && "启用".equals(item.status()))
                .findFirst().orElse(null);
        if (account == null || !passwordEncoder.matches(request.password(), account.password())) {
            return Result.fail("用户名或密码错误");
        }
        var user = new UserView(account.username(), account.displayName(), account.role());
        var token = jwtUtil.generate(user);
        store.addLog(null, account.username(), "用户登录", "系统认证");
        return Result.ok(Map.of(
                "username", user.username(),
                "displayName", user.displayName(),
                "role", user.role(),
                "token", token
        ));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        if (request.username().length() < 3 || request.password().length() < 6) {
            return Result.fail("用户名至少3位，密码至少6位");
        }
        if (store.users().stream().anyMatch(user -> user.username().equals(request.username()))) {
            return Result.fail("用户名已存在");
        }
        var now = LocalDateTime.now();
        var user = new SystemUser(store.nextUserId(), request.username(), request.displayName(),
                passwordEncoder.encode(request.password()), "普通用户", request.phone(), "启用", now, now);
        store.users().add(user);
        store.persist();
        var view = new UserView(user.username(), user.displayName(), user.role());
        store.addLog(null, user.username(), "用户注册", "系统认证");
        return Result.ok(Map.of(
                "username", view.username(),
                "displayName", view.displayName(),
                "role", view.role(),
                "token", jwtUtil.generate(view)
        ));
    }

    @GetMapping("/me")
    public Result<Object> me(HttpServletRequest request) {
        var user = request.getAttribute("user");
        if (user == null) return Result.fail("未登录");
        var view = (UserView) user;
        var account = store.users().stream()
                .filter(u -> u.username().equals(view.username()))
                .findFirst().orElse(null);
        if (account == null) return Result.ok(view);
        return Result.ok(Map.of(
                "username", account.username(),
                "displayName", account.displayName(),
                "role", account.role(),
                "phone", account.phone() != null ? account.phone() : ""
        ));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request, HttpServletRequest req) {
        var view = (UserView) req.getAttribute("user");
        if (view == null) return Result.fail("请先登录");
        var idx = -1;
        for (int i = 0; i < store.users().size(); i++) {
            if (store.users().get(i).username().equals(view.username())) { idx = i; break; }
        }
        if (idx < 0) return Result.fail("用户不存在");
        var account = store.users().get(idx);
        if (!passwordEncoder.matches(request.oldPassword(), account.password())) {
            return Result.fail("旧密码错误");
        }
        if (request.newPassword().length() < 6) {
            return Result.fail("新密码至少6位");
        }
        var updated = new SystemUser(account.id(), account.username(), account.displayName(),
                passwordEncoder.encode(request.newPassword()), account.role(), account.phone(),
                account.status(), account.createTime(), LocalDateTime.now());
        store.users().set(idx, updated);
        store.persist();
        store.addLog(null, account.username(), "修改密码", "系统认证");
        return Result.ok();
    }

    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, HttpServletRequest req) {
        var view = (UserView) req.getAttribute("user");
        if (view == null) return Result.fail("请先登录");
        var idx = -1;
        for (int i = 0; i < store.users().size(); i++) {
            if (store.users().get(i).username().equals(view.username())) { idx = i; break; }
        }
        if (idx < 0) return Result.fail("用户不存在");
        var account = store.users().get(idx);
        var updated = new SystemUser(account.id(), account.username(),
                request.displayName() != null ? request.displayName() : account.displayName(),
                account.password(), account.role(),
                request.phone() != null ? request.phone() : account.phone(),
                account.status(), account.createTime(), LocalDateTime.now());
        store.users().set(idx, updated);
        store.persist();
        var newView = new UserView(updated.username(), updated.displayName(), updated.role());
        return Result.ok(Map.of(
                "username", newView.username(),
                "displayName", newView.displayName(),
                "role", newView.role(),
                "token", jwtUtil.generate(newView)
        ));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest req) {
        var view = (UserView) req.getAttribute("user");
        if (view != null) store.addLog(null, view.username(), "用户登出", "系统认证");
        return Result.ok();
    }
}
