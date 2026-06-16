package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.Notice;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpSession;
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
    public Result<Notice> create(@Valid @RequestBody Notice input, HttpSession session) {
        var user = (UserView) session.getAttribute("user");
        var notice = new Notice(store.nextNoticeId(), input.title(), input.type(),
                input.content(), null, input.status() != null ? input.status() : "已发布", LocalDateTime.now());
        store.notices().add(notice);
        store.persist();
        store.addLog(null, user.username(), "发布公告: " + notice.title(), "通知公告");
        return Result.ok(notice);
    }

    @PutMapping("/{id}")
    public Result<Notice> update(@PathVariable Long id, @Valid @RequestBody Notice input, HttpSession session) {
        var idx = findIndex(id);
        var old = store.notices().get(idx);
        var notice = new Notice(id, input.title(), input.type(), input.content(), old.publisherId(),
                input.status() != null ? input.status() : old.status(), old.publishedAt());
        store.notices().set(idx, notice);
        store.persist();
        var user = (UserView) session.getAttribute("user");
        store.addLog(null, user.username(), "修改公告: " + notice.title(), "通知公告");
        return Result.ok(notice);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        var removed = store.notices().get(findIndex(id));
        store.notices().remove(findIndex(id));
        store.persist();
        var user = (UserView) session.getAttribute("user");
        store.addLog(null, user.username(), "删除公告: " + removed.title(), "通知公告");
        return Result.ok();
    }

    private int findIndex(Long id) {
        for (int i = 0; i < store.notices().size(); i++) {
            if (store.notices().get(i).id().equals(id)) return i;
        }
        throw new IllegalArgumentException("公告不存在");
    }
}
