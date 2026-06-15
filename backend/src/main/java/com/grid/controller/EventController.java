package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.Event;
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
@RequestMapping("/api/events")
public class EventController {
    private static final Set<String> STATUSES = Set.of("待处理", "处理中", "已办结");
    private final DataStore store;

    public EventController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<Event>> list(@RequestParam(defaultValue = "") String status, HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        return Result.ok(store.events().stream()
                .filter(event -> status.isBlank() || status.equals(event.status()))
                .filter(event -> "管理员".equals(user.role()) || user.username().equals(event.ownerUsername()))
                .sorted(Comparator.comparing(Event::createdAt).reversed())
                .toList());
    }

    @PostMapping
    public Result<Event> create(@Valid @RequestBody Event input, HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        var id = store.nextEventId();
        var now = LocalDateTime.now();
        var event = new Event(id, "EV-" + now.getYear() + String.format("%03d", id), input.title(), input.category(),
                input.gridName(), "管理员".equals(user.role()) ? input.reporter() : user.displayName(),
                user.username(), input.description(), input.imageUrl(), "待处理", now, now);
        store.events().add(event);
        store.persist();
        return Result.ok(event);
    }

    @PutMapping("/{id}/status")
    public Result<Event> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (!STATUSES.contains(request.status())) throw new IllegalArgumentException("无效的事件状态");
        var index = findIndex(id);
        var old = store.events().get(index);
        var updated = new Event(old.id(), old.code(), old.title(), old.category(), old.gridName(), old.reporter(),
                old.ownerUsername(), old.description(), old.imageUrl(), request.status(), old.createdAt(), LocalDateTime.now());
        store.events().set(index, updated);
        store.persist();
        return Result.ok(updated);
    }

    public record StatusRequest(String status) {}

    private int findIndex(Long id) {
        for (int i = 0; i < store.events().size(); i++) {
            if (store.events().get(i).id().equals(id)) return i;
        }
        throw new IllegalArgumentException("事件不存在");
    }
}
