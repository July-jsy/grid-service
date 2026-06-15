package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.Notice;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final DataStore store;

    public NoticeController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<Notice>> list() {
        return Result.ok(store.notices().stream()
                .sorted(Comparator.comparing(Notice::publishedAt).reversed()).toList());
    }

    @PostMapping
    public Result<Notice> create(@Valid @RequestBody Notice input) {
        var notice = new Notice(store.nextNoticeId(), input.title(), input.category(), input.content(), LocalDateTime.now());
        store.notices().add(notice);
        store.persist();
        return Result.ok(notice);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!store.notices().removeIf(notice -> notice.id().equals(id))) {
            throw new IllegalArgumentException("公告不存在");
        }
        store.persist();
        return Result.ok();
    }
}
