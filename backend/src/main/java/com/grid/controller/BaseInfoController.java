package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.House;
import com.grid.model.Resident;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-info")
public class BaseInfoController {
    private final DataStore store;

    public BaseInfoController(DataStore store) {
        this.store = store;
    }

    @GetMapping("/residents")
    public Result<List<Resident>> residents() {
        return Result.ok(List.copyOf(store.residents()));
    }

    @PostMapping("/residents")
    public Result<Resident> createResident(@Valid @RequestBody Resident input) {
        var resident = new Resident(store.nextResidentId(), input.name(), input.gender(), input.phone(),
                input.gridName(), input.address(), input.type());
        store.residents().add(resident);
        store.persist();
        return Result.ok(resident);
    }

    @DeleteMapping("/residents/{id}")
    public Result<Void> deleteResident(@PathVariable Long id) {
        if (!store.residents().removeIf(item -> item.id().equals(id))) throw new IllegalArgumentException("居民档案不存在");
        store.persist();
        return Result.ok();
    }

    @GetMapping("/houses")
    public Result<List<House>> houses() {
        return Result.ok(List.copyOf(store.houses()));
    }

    @PostMapping("/houses")
    public Result<House> createHouse(@Valid @RequestBody House input) {
        var house = new House(store.nextHouseId(), input.address(), input.gridName(), input.propertyType(),
                input.rentalStatus(), input.ownerName(), input.residentCount());
        store.houses().add(house);
        store.persist();
        return Result.ok(house);
    }

    @DeleteMapping("/houses/{id}")
    public Result<Void> deleteHouse(@PathVariable Long id) {
        if (!store.houses().removeIf(item -> item.id().equals(id))) throw new IllegalArgumentException("房屋档案不存在");
        store.persist();
        return Result.ok();
    }
}
