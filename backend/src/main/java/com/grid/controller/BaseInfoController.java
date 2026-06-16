package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.House;
import com.grid.model.Resident;
import com.grid.model.UserView;
import com.grid.service.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/base-info")
public class BaseInfoController {
    private final DataStore store;

    public BaseInfoController(DataStore store) {
        this.store = store;
    }

    private UserView currentUser(HttpServletRequest req) {
        return (UserView) req.getAttribute("user");
    }

    // ===== 居民 =====
    @GetMapping("/residents")
    public Result<List<Resident>> residents(@RequestParam(defaultValue = "") String keyword) {
        var list = store.residents().stream()
                .filter(r -> keyword.isBlank() || r.name().contains(keyword) || r.address().contains(keyword))
                .sorted(Comparator.comparing(Resident::id))
                .toList();
        return Result.ok(list);
    }

    @PostMapping("/residents")
    public Result<Resident> createResident(@Valid @RequestBody Resident input, HttpServletRequest req) {
        var resident = new Resident(store.nextResidentId(), input.name(), input.gender(), input.idCard(),
                input.phone(), input.gridName(), input.gridId(), input.address(), input.houseId(),
                input.residentType(), input.remark());
        store.residents().add(resident);
        store.persist();
        store.addLog(null, currentUser(req).username(), "新增居民: " + resident.name(), "基础信息");
        return Result.ok(resident);
    }

    @PutMapping("/residents/{id}")
    public Result<Resident> updateResident(@PathVariable Long id, @Valid @RequestBody Resident input, HttpServletRequest req) {
        var idx = findResidentIndex(id);
        var resident = new Resident(id, input.name(), input.gender(), input.idCard(),
                input.phone(), input.gridName(), input.gridId(), input.address(), input.houseId(),
                input.residentType(), input.remark());
        store.residents().set(idx, resident);
        store.persist();
        store.addLog(null, currentUser(req).username(), "修改居民: " + resident.name(), "基础信息");
        return Result.ok(resident);
    }

    @DeleteMapping("/residents/{id}")
    public Result<Void> deleteResident(@PathVariable Long id, HttpServletRequest req) {
        var removed = store.residents().stream().filter(r -> r.id().equals(id)).findFirst().orElse(null);
        if (!store.residents().removeIf(item -> item.id().equals(id)))
            throw new IllegalArgumentException("居民档案不存在");
        store.persist();
        store.addLog(null, currentUser(req).username(), "删除居民: " + (removed != null ? removed.name() : id), "基础信息");
        return Result.ok();
    }

    // ===== 房屋 =====
    @GetMapping("/houses")
    public Result<List<House>> houses(@RequestParam(defaultValue = "") String keyword) {
        var list = store.houses().stream()
                .filter(h -> keyword.isBlank() || h.address().contains(keyword) || h.gridName().contains(keyword))
                .sorted(Comparator.comparing(House::id))
                .toList();
        return Result.ok(list);
    }

    @PostMapping("/houses")
    public Result<House> createHouse(@Valid @RequestBody House input, HttpServletRequest req) {
        var house = new House(store.nextHouseId(), input.houseCode(), input.address(), input.building(),
                input.unit(), input.roomNumber(), input.gridName(), input.gridId(), input.houseType(),
                input.usageStatus(), input.ownerName(), input.residentCount(), input.longitude(), input.latitude());
        store.houses().add(house);
        store.persist();
        store.addLog(null, currentUser(req).username(), "新增房屋: " + house.address(), "基础信息");
        return Result.ok(house);
    }

    @PutMapping("/houses/{id}")
    public Result<House> updateHouse(@PathVariable Long id, @Valid @RequestBody House input, HttpServletRequest req) {
        var idx = findHouseIndex(id);
        var house = new House(id, input.houseCode(), input.address(), input.building(),
                input.unit(), input.roomNumber(), input.gridName(), input.gridId(), input.houseType(),
                input.usageStatus(), input.ownerName(), input.residentCount(), input.longitude(), input.latitude());
        store.houses().set(idx, house);
        store.persist();
        store.addLog(null, currentUser(req).username(), "修改房屋: " + house.address(), "基础信息");
        return Result.ok(house);
    }

    @DeleteMapping("/houses/{id}")
    public Result<Void> deleteHouse(@PathVariable Long id, HttpServletRequest req) {
        var removed = store.houses().stream().filter(h -> h.id().equals(id)).findFirst().orElse(null);
        if (!store.houses().removeIf(item -> item.id().equals(id)))
            throw new IllegalArgumentException("房屋档案不存在");
        store.persist();
        store.addLog(null, currentUser(req).username(), "删除房屋: " + (removed != null ? removed.address() : id), "基础信息");
        return Result.ok();
    }

    private int findResidentIndex(Long id) {
        for (int i = 0; i < store.residents().size(); i++)
            if (store.residents().get(i).id().equals(id)) return i;
        throw new IllegalArgumentException("居民档案不存在");
    }

    private int findHouseIndex(Long id) {
        for (int i = 0; i < store.houses().size(); i++)
            if (store.houses().get(i).id().equals(id)) return i;
        throw new IllegalArgumentException("房屋档案不存在");
    }
}
