package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.SystemUser;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SystemUserController {
    private final DataStore store;

    public SystemUserController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<SystemUser>> list() {
        return Result.ok(List.copyOf(store.users()));
    }

    @PostMapping
    public Result<SystemUser> create(@Valid @RequestBody SystemUser input) {
        if (store.users().stream().anyMatch(user -> user.username().equals(input.username()))) {
            throw new IllegalArgumentException("用户名已存在");
        }
        var user = new SystemUser(store.nextUserId(), input.username(), input.displayName(),
                input.role(), input.phone(), input.status() == null ? "启用" : input.status());
        store.users().add(user);
        store.persist();
        return Result.ok(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!store.users().removeIf(user -> user.id().equals(id) && !"admin".equals(user.username()))) {
            throw new IllegalArgumentException("用户不存在或管理员账号不可删除");
        }
        store.persist();
        return Result.ok();
    }
}
