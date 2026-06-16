package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DataStore store;

    public DashboardController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<Map<String, Object>> overview() {
        var events = store.events();
        long completed = events.stream().filter(event -> "已办结".equals(event.status())).count();
        long pending = events.stream().filter(event -> "待处理".equals(event.status())).count();
        long processing = events.stream().filter(event -> "处理中".equals(event.status())).count();
        var data = new LinkedHashMap<String, Object>();
        data.put("gridCount", store.grids().size());
        data.put("residentCount", store.residents().size());
        data.put("eventCount", events.size());
        data.put("pendingCount", pending);
        data.put("processingCount", processing);
        data.put("completedCount", completed);
        data.put("completionRate", events.isEmpty() ? 0 : Math.round(completed * 100.0 / events.size()));
        data.put("serviceCount", store.serviceItems().size());
        data.put("applicationCount", store.applications().size());
        data.put("houseCount", store.houses().size());
        data.put("noticeCount", store.notices().size());
        data.put("statusDistribution", Map.of(
                "待处理", pending, "处理中", processing, "已办结", completed));
        data.put("categoryDistribution", events.stream()
                .collect(Collectors.groupingBy(event -> event.category(), Collectors.counting())));
        return Result.ok(data);
    }

    @GetMapping("/my-stats")
    public Result<Map<String, Object>> myStats(HttpServletRequest req) {
        var user = (UserView) req.getAttribute("user");
        if (user == null) return Result.fail("请先登录");
        var username = user.username();
        long myEvents = store.events().stream().filter(e -> username.equals(e.ownerUsername())).count();
        long myPendingEvents = store.events().stream()
                .filter(e -> username.equals(e.ownerUsername()) && !"已办结".equals(e.status())).count();
        long myApplications = store.applications().stream()
                .filter(a -> username.equals(a.ownerUsername())).count();
        long myPendingApplications = store.applications().stream()
                .filter(a -> username.equals(a.ownerUsername()) && !"已办结".equals(a.status()) && !"已驳回".equals(a.status())).count();
        var data = new LinkedHashMap<String, Object>();
        data.put("myEventCount", myEvents);
        data.put("myPendingEventCount", myPendingEvents);
        data.put("myApplicationCount", myApplications);
        data.put("myPendingApplicationCount", myPendingApplications);
        return Result.ok(data);
    }
}
