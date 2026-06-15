package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.SystemUser;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class SystemUserController {
    private static final Set<String> ROLES = Set.of("管理员", "普通用户");
    private final DataStore store;
    private final PasswordEncoder passwordEncoder;

    public SystemUserController(DataStore store, PasswordEncoder passwordEncoder) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Result<List<UserAccountView>> list(HttpSession session) {
        requireAdmin(session);
        return Result.ok(store.users().stream().map(UserAccountView::from).toList());
    }

    @PostMapping
    public Result<UserAccountView> create(@Valid @RequestBody SystemUser input, HttpSession session) {
        requireAdmin(session);
        if (store.users().stream().anyMatch(user -> user.username().equals(input.username()))) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!ROLES.contains(input.role())) throw new IllegalArgumentException("无效的用户角色");
        var password = input.password() == null || input.password().isBlank() ? "user123" : input.password();
        var user = new SystemUser(store.nextUserId(), input.username(), input.displayName(), passwordEncoder.encode(password),
                input.role(), input.phone(), input.status() == null ? "启用" : input.status());
        store.users().add(user);
        store.persist();
        return Result.ok(UserAccountView.from(user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        if (!store.users().removeIf(user -> user.id().equals(id) && !"admin".equals(user.username()))) {
            throw new IllegalArgumentException("用户不存在或管理员账号不可删除");
        }
        store.persist();
        return Result.ok();
    }

    private void requireAdmin(HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.role())) {
            throw new IllegalArgumentException("仅管理员可访问用户管理");
        }
    }

    public record UserAccountView(Long id, String username, String displayName, String role, String phone, String status) {
        static UserAccountView from(SystemUser user) {
            return new UserAccountView(user.id(), user.username(), user.displayName(), user.role(), user.phone(), user.status());
        }
    }
}
