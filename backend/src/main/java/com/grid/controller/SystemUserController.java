package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.SystemUser;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
        var now = LocalDateTime.now();
        var user = new SystemUser(store.nextUserId(), input.username(), input.displayName(),
                passwordEncoder.encode(password), input.role(), input.phone(),
                input.status() == null ? "启用" : input.status(), now, now);
        store.users().add(user);
        store.persist();
        var sessionUser = (UserView) session.getAttribute("user");
        store.addLog(null, sessionUser.username(), "新增用户: " + user.username(), "用户管理");
        return Result.ok(UserAccountView.from(user));
    }

    @PutMapping("/{id}")
    public Result<UserAccountView> update(@PathVariable Long id, @Valid @RequestBody SystemUser input, HttpSession session) {
        requireAdmin(session);
        var idx = findIndex(id);
        var old = store.users().get(idx);
        if (!old.username().equals(input.username())
                && store.users().stream().anyMatch(u -> u.username().equals(input.username()))) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!ROLES.contains(input.role())) throw new IllegalArgumentException("无效的用户角色");
        var password = input.password() != null && !input.password().isBlank()
                ? passwordEncoder.encode(input.password()) : old.password();
        var updated = new SystemUser(id, input.username(), input.displayName(), password,
                input.role(), input.phone(), input.status() == null ? old.status() : input.status(),
                old.createTime(), LocalDateTime.now());
        store.users().set(idx, updated);
        store.persist();
        var sessionUser = (UserView) session.getAttribute("user");
        store.addLog(null, sessionUser.username(), "修改用户: " + updated.username(), "用户管理");
        return Result.ok(UserAccountView.from(updated));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        var user = store.users().get(findIndex(id));
        if ("admin".equals(user.username())) throw new IllegalArgumentException("管理员账号不可删除");
        store.users().remove(findIndex(id));
        store.persist();
        var sessionUser = (UserView) session.getAttribute("user");
        store.addLog(null, sessionUser.username(), "删除用户: " + user.username(), "用户管理");
        return Result.ok();
    }

    @GetMapping("/logs")
    public Result<List<com.grid.model.OperationLog>> logs(@RequestParam(defaultValue = "50") int limit, HttpSession session) {
        requireAdmin(session);
        var logs = store.operationLogs().stream()
                .sorted(java.util.Comparator.comparing(com.grid.model.OperationLog::createTime).reversed())
                .limit(limit)
                .toList();
        return Result.ok(logs);
    }

    private void requireAdmin(HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        if (user == null || !"管理员".equals(user.role())) {
            throw new IllegalArgumentException("仅管理员可访问用户管理");
        }
    }

    private int findIndex(Long id) {
        for (int i = 0; i < store.users().size(); i++) {
            if (store.users().get(i).id().equals(id)) return i;
        }
        throw new IllegalArgumentException("用户不存在");
    }

    public record UserAccountView(Long id, String username, String displayName, String role, String phone, String status, LocalDateTime createTime) {
        static UserAccountView from(SystemUser user) {
            return new UserAccountView(user.id(), user.username(), user.displayName(), user.role(), user.phone(), user.status(), user.createTime());
        }
    }
}
