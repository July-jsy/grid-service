package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.ServiceApplication;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/service-applications")
public class ServiceApplicationController {
    private static final Set<String> STATUSES = Set.of("待受理", "办理中", "已办结", "已驳回");
    private final DataStore store;

    public ServiceApplicationController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<ServiceApplication>> list(HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        return Result.ok(store.applications().stream()
                .filter(item -> "管理员".equals(user.role()) || user.username().equals(item.ownerUsername()))
                .sorted(Comparator.comparing(ServiceApplication::createdAt).reversed()).toList());
    }

    @PostMapping
    public Result<ServiceApplication> create(@Valid @RequestBody ServiceApplication input, HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        var id = store.nextApplicationId();
        var now = LocalDateTime.now();
        var application = new ServiceApplication(id, "SA-" + now.getYear() + String.format("%03d", id),
                null, input.serviceItemId(), input.itemName(),
                "管理员".equals(user.role()) ? input.applicantName() : user.displayName(),
                input.applicantPhone(), user.username(), input.content(),
                input.attachment(), "待受理", null, now, now);
        store.applications().add(application);
        store.persist();
        store.addLog(null, user.username(), "提交服务申请: " + application.itemName(), "便民服务");
        return Result.ok(application);
    }

    @PutMapping("/{id}/status")
    public Result<ServiceApplication> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request, HttpSession session) {
        if (!STATUSES.contains(request.status())) throw new IllegalArgumentException("无效的申请状态");
        for (int i = 0; i < store.applications().size(); i++) {
            var old = store.applications().get(i);
            if (old.id().equals(id)) {
                var updated = new ServiceApplication(old.id(), old.code(), old.userId(), old.serviceItemId(),
                        old.itemName(), old.applicantName(), old.applicantPhone(), old.ownerUsername(),
                        old.content(), old.attachment(), request.status(),
                        request.handleResult() != null ? request.handleResult() : old.handleResult(),
                        old.createdAt(), LocalDateTime.now());
                store.applications().set(i, updated);
                store.persist();
                var user = (UserView) session.getAttribute("user");
                store.addLog(null, user.username(), "更新申请状态: " + old.itemName() + " → " + request.status(), "便民服务");
                return Result.ok(updated);
            }
        }
        throw new IllegalArgumentException("服务申请不存在");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        var removed = store.applications().stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
        if (!store.applications().removeIf(item -> item.id().equals(id)))
            throw new IllegalArgumentException("服务申请不存在");
        store.persist();
        var user = (UserView) session.getAttribute("user");
        store.addLog(null, user.username(), "删除服务申请: " + (removed != null ? removed.itemName() : id), "便民服务");
        return Result.ok();
    }

    public record StatusRequest(String status, String handleResult) {}
}
