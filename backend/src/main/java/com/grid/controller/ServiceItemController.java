package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.ServiceItem;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
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

    private UserView currentUser(HttpServletRequest req) {
        return (UserView) req.getAttribute("user");
    }

    @GetMapping
    public Result<List<ServiceItem>> list() {
        return Result.ok(List.copyOf(store.serviceItems()));
    }

    @PostMapping
    public Result<ServiceItem> create(@Valid @RequestBody ServiceItem input, HttpServletRequest req) {
        var item = new ServiceItem(store.nextServiceId(), input.name(), input.category(), input.department(),
                input.description(), input.requiredMaterials(), input.process(), input.timeLimit(),
                input.status() == null ? "启用" : input.status());
        store.serviceItems().add(item);
        store.persist();
        store.addLog(null, currentUser(req).username(), "新增服务事项: " + item.name(), "便民服务");
        return Result.ok(item);
    }

    @PutMapping("/{id}")
    public Result<ServiceItem> update(@PathVariable Long id, @Valid @RequestBody ServiceItem input, HttpServletRequest req) {
        var idx = findIndex(id);
        var item = new ServiceItem(id, input.name(), input.category(), input.department(),
                input.description(), input.requiredMaterials(), input.process(), input.timeLimit(),
                input.status() == null ? "启用" : input.status());
        store.serviceItems().set(idx, item);
        store.persist();
        store.addLog(null, currentUser(req).username(), "修改服务事项: " + item.name(), "便民服务");
        return Result.ok(item);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        var removed = store.serviceItems().get(findIndex(id));
        store.serviceItems().remove(findIndex(id));
        store.persist();
        store.addLog(null, currentUser(req).username(), "删除服务事项: " + removed.name(), "便民服务");
        return Result.ok();
    }

    private int findIndex(Long id) {
        for (int i = 0; i < store.serviceItems().size(); i++)
            if (store.serviceItems().get(i).id().equals(id)) return i;
        throw new IllegalArgumentException("服务事项不存在");
    }
}
