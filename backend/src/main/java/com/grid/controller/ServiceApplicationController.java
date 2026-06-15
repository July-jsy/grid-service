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
    private static final Set<String> STATUSES = Set.of("待受理", "办理中", "已办结");
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
                input.itemName(), "管理员".equals(user.role()) ? input.applicant() : user.displayName(),
                user.username(), input.phone(), input.materialsNote(), "待受理", now, now);
        store.applications().add(application);
        store.persist();
        return Result.ok(application);
    }

    @PutMapping("/{id}/status")
    public Result<ServiceApplication> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (!STATUSES.contains(request.status())) throw new IllegalArgumentException("无效的申请状态");
        for (int i = 0; i < store.applications().size(); i++) {
            var old = store.applications().get(i);
            if (old.id().equals(id)) {
                var updated = new ServiceApplication(old.id(), old.code(), old.itemName(), old.applicant(),
                        old.ownerUsername(), old.phone(), old.materialsNote(), request.status(), old.createdAt(), LocalDateTime.now());
                store.applications().set(i, updated);
                store.persist();
                return Result.ok(updated);
            }
        }
        throw new IllegalArgumentException("服务申请不存在");
    }

    public record StatusRequest(String status) {}
}
