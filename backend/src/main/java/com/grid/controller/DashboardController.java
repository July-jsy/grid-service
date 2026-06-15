package com.grid.controller;

import com.grid.common.Result;
import com.grid.service.DataStore;
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
        var data = new LinkedHashMap<String, Object>();
        data.put("gridCount", store.grids().size());
        data.put("residentCount", store.grids().stream().mapToInt(grid -> grid.residentCount()).sum());
        data.put("eventCount", events.size());
        data.put("completedCount", completed);
        data.put("completionRate", events.isEmpty() ? 0 : Math.round(completed * 100.0 / events.size()));
        data.put("serviceCount", store.serviceItems().size());
        data.put("applicationCount", store.applications().size());
        data.put("houseCount", store.houses().size());
        data.put("statusDistribution", events.stream().collect(Collectors.groupingBy(event -> event.status(), Collectors.counting())));
        data.put("categoryDistribution", events.stream().collect(Collectors.groupingBy(event -> event.category(), Collectors.counting())));
        return Result.ok(data);
    }
}
