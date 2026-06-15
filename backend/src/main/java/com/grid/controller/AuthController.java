package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.SystemUser;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final DataStore store;
    private final PasswordEncoder passwordEncoder;

    public AuthController(DataStore store, PasswordEncoder passwordEncoder) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "姓名不能为空") String displayName,
            @NotBlank(message = "密码不能为空") String password,
            String phone
    ) {}

    @PostMapping("/login")
    public Result<UserView> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        var account = store.users().stream()
                .filter(item -> item.username().equals(request.username()) && "启用".equals(item.status()))
                .findFirst().orElse(null);
        if (account == null || !passwordEncoder.matches(request.password(), account.password())) {
            return Result.fail("用户名或密码错误");
        }
        var user = new UserView(account.username(), account.displayName(), account.role());
        session.setAttribute("user", user);
        return Result.ok(user);
    }

    @PostMapping("/register")
    public Result<UserView> register(@Valid @RequestBody RegisterRequest request) {
        if (request.username().length() < 3 || request.password().length() < 6) {
            return Result.fail("用户名至少3位，密码至少6位");
        }
        if (store.users().stream().anyMatch(user -> user.username().equals(request.username()))) {
            return Result.fail("用户名已存在");
        }
        var user = new SystemUser(store.nextUserId(), request.username(), request.displayName(),
                passwordEncoder.encode(request.password()), "普通用户", request.phone(), "启用");
        store.users().add(user);
        store.persist();
        return Result.ok(new UserView(user.username(), user.displayName(), user.role()));
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
