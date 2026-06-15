package com.grid.controller;

import com.grid.common.Result;
import com.grid.model.Grid;
import com.grid.service.DataStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/grids")
public class GridController {
    private final DataStore store;

    public GridController(DataStore store) {
        this.store = store;
    }

    @GetMapping
    public Result<List<Grid>> list(@RequestParam(defaultValue = "") String keyword) {
        var result = store.grids().stream()
                .filter(grid -> keyword.isBlank() || grid.name().contains(keyword) || grid.community().contains(keyword))
                .sorted(Comparator.comparing(Grid::id))
                .toList();
        return Result.ok(result);
    }

    @PostMapping
    public Result<Grid> create(@Valid @RequestBody Grid input) {
        var grid = new Grid(store.nextGridId(), input.code(), input.name(), input.community(), input.description(),
                input.staffName(), input.staffPhone(), input.residentCount());
        store.grids().add(grid);
        store.persist();
        return Result.ok(grid);
    }

    @PutMapping("/{id}")
    public Result<Grid> update(@PathVariable Long id, @Valid @RequestBody Grid input) {
        var index = findIndex(id);
        var grid = new Grid(id, input.code(), input.name(), input.community(), input.description(),
                input.staffName(), input.staffPhone(), input.residentCount());
        store.grids().set(index, grid);
        store.persist();
        return Result.ok(grid);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        store.grids().remove(findIndex(id));
        store.persist();
        return Result.ok();
    }

    private int findIndex(Long id) {
        for (int i = 0; i < store.grids().size(); i++) {
            if (store.grids().get(i).id().equals(id)) return i;
        }
        throw new IllegalArgumentException("网格不存在");
    }
}
