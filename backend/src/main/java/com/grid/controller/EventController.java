package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.Event;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private static final Set<String> STATUSES = Set.of("待处理", "处理中", "已办结");
    private final DataStore store;

    public EventController(DataStore store) {
        this.store = store;
    }

    private UserView currentUser(HttpServletRequest req) {
        return (UserView) req.getAttribute("user");
    }

    @GetMapping
    public Result<List<Event>> list(@RequestParam(defaultValue = "") String status, HttpServletRequest req) {
        var user = currentUser(req);
        return Result.ok(store.events().stream()
                .filter(event -> status.isBlank() || status.equals(event.status()))
                .filter(event -> "管理员".equals(user.role()) || user.username().equals(event.ownerUsername()))
                .sorted(Comparator.comparing(Event::createdAt).reversed())
                .toList());
    }

    @PostMapping
    public Result<Event> create(@Valid @RequestBody Event input, HttpServletRequest req) {
        var user = currentUser(req);
        var id = store.nextEventId();
        var now = LocalDateTime.now();
        var event = new Event(id, "EV-" + now.getYear() + String.format("%03d", id), input.title(), input.category(),
                input.gridName(), "管理员".equals(user.role()) ? input.reporter() : user.displayName(),
                user.username(), input.description(), input.imageUrl(), input.address(),
                input.longitude(), input.latitude(), "待处理", null, null, now, now);
        store.events().add(event);
        store.persist();
        store.addLog(null, user.username(), "上报事件: " + event.title(), "事件处置");
        return Result.ok(event);
    }

    @PutMapping("/{id}/status")
    public Result<Event> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request, HttpServletRequest req) {
        if (!STATUSES.contains(request.status())) throw new IllegalArgumentException("无效的事件状态");
        var index = findIndex(id);
        var old = store.events().get(index);
        var user = currentUser(req);
        var updated = new Event(old.id(), old.code(), old.title(), old.category(), old.gridName(), old.reporter(),
                old.ownerUsername(), old.description(), old.imageUrl(), old.address(),
                old.longitude(), old.latitude(), request.status(),
                "管理员".equals(user.role()) ? request.handlerId() : old.handlerId(),
                request.handleResult() != null ? request.handleResult() : old.handleResult(),
                old.createdAt(), LocalDateTime.now());
        store.events().set(index, updated);
        store.persist();
        store.addLog(null, user.username(), "更新事件状态: " + old.title() + " → " + request.status(), "事件处置");
        return Result.ok(updated);
    }

    @PutMapping("/{id}")
    public Result<Event> update(@PathVariable Long id, @Valid @RequestBody Event input, HttpServletRequest req) {
        var index = findIndex(id);
        var old = store.events().get(index);
        var updated = new Event(id, old.code(), input.title(), input.category(), input.gridName(),
                input.reporter(), old.ownerUsername(), input.description(), input.imageUrl(), input.address(),
                input.longitude(), input.latitude(), old.status(), old.handlerId(), old.handleResult(),
                old.createdAt(), LocalDateTime.now());
        store.events().set(index, updated);
        store.persist();
        var user = currentUser(req);
        store.addLog(null, user.username(), "编辑事件: " + updated.title(), "事件处置");
        return Result.ok(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        var removed = store.events().get(findIndex(id));
        store.events().remove(findIndex(id));
        store.persist();
        var user = currentUser(req);
        store.addLog(null, user.username(), "删除事件: " + removed.title(), "事件处置");
        return Result.ok();
    }

    public record StatusRequest(String status, Long handlerId, String handleResult) {}

    private int findIndex(Long id) {
        for (int i = 0; i < store.events().size(); i++) {
            if (store.events().get(i).id().equals(id)) return i;
        }
        throw new IllegalArgumentException("事件不存在");
    }
}
