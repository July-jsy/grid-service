package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.ServiceItem;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-items")
public class ServiceItemController {
    private final DataStore store;

    public ServiceItemController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<ServiceItem>> list() {
        return Result.ok(List.copyOf(store.serviceItems()));
    }

    @PostMapping
    public Result<ServiceItem> create(@Valid @RequestBody ServiceItem input) {
        var item = new ServiceItem(store.nextServiceId(), input.name(), input.category(), input.department(),
                input.deadlineDays(), input.materials(), input.status() == null ? "启用" : input.status());
        store.serviceItems().add(item);
        store.persist();
        return Result.ok(item);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!store.serviceItems().removeIf(item -> item.id().equals(id))) {
            throw new IllegalArgumentException("服务事项不存在");
        }
        store.persist();
        return Result.ok();
    }
}
