package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.ServiceApplication;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/service-applications")
public class ServiceApplicationController {
    private final DataStore store;

    public ServiceApplicationController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<ServiceApplication>> list() {
        return Result.ok(store.applications().stream()
                .sorted(Comparator.comparing(ServiceApplication::createdAt).reversed()).toList());
    }

    @PostMapping
    public Result<ServiceApplication> create(@Valid @RequestBody ServiceApplication input) {
        var id = store.nextApplicationId();
        var now = LocalDateTime.now();
        var application = new ServiceApplication(id, "SA-" + now.getYear() + String.format("%03d", id),
                input.itemName(), input.applicant(), input.phone(), input.materialsNote(), "待受理", now, now);
        store.applications().add(application);
        store.persist();
        return Result.ok(application);
    }

    @PutMapping("/{id}/status")
    public Result<ServiceApplication> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        for (int i = 0; i < store.applications().size(); i++) {
            var old = store.applications().get(i);
            if (old.id().equals(id)) {
                var updated = new ServiceApplication(old.id(), old.code(), old.itemName(), old.applicant(),
                        old.phone(), old.materialsNote(), request.status(), old.createdAt(), LocalDateTime.now());
                store.applications().set(i, updated);
                store.persist();
                return Result.ok(updated);
            }
        }
        throw new IllegalArgumentException("服务申请不存在");
    }

    public record StatusRequest(String status) {}
}
