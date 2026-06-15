package com.grid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grid.model.Event;
import com.grid.model.Grid;
import com.grid.model.House;
import com.grid.model.Resident;
import com.grid.model.ServiceApplication;
import com.grid.model.ServiceItem;
import com.grid.model.SystemUser;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final List<Grid> grids = new ArrayList<>();
    private final List<ServiceItem> serviceItems = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final List<Resident> residents = new ArrayList<>();
    private final List<House> houses = new ArrayList<>();
    private final List<ServiceApplication> applications = new ArrayList<>();
    private final List<SystemUser> users = new ArrayList<>();
    private final AtomicLong gridId = new AtomicLong();
    private final AtomicLong serviceId = new AtomicLong();
    private final AtomicLong eventId = new AtomicLong();
    private final AtomicLong residentId = new AtomicLong();
    private final AtomicLong houseId = new AtomicLong();
    private final AtomicLong applicationId = new AtomicLong();
    private final AtomicLong userId = new AtomicLong();

    public DataStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS app_state (state_key TEXT PRIMARY KEY, state_value TEXT NOT NULL)");
        loadState();
        if (!grids.isEmpty()) {
            resetSequences();
            return;
        }

        grids.add(new Grid(gridId.incrementAndGet(), "WG-001", "春风里第一网格", "春风里社区", "覆盖春风路1-8号楼", "张晓梅", "138****1201", 1260));
        grids.add(new Grid(gridId.incrementAndGet(), "WG-002", "春风里第二网格", "春风里社区", "覆盖春风路9-16号楼", "李建国", "138****1202", 980));
        grids.add(new Grid(gridId.incrementAndGet(), "WG-003", "幸福家园网格", "幸福家园社区", "幸福家园小区全域", "王芳", "138****1203", 1530));

        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "低保申请", "民政服务", "社会事务办公室", 15, "身份证、户口簿、收入证明", "启用"));
        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "高龄津贴申请", "民政服务", "社会事务办公室", 10, "身份证、银行卡", "启用"));
        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "养老保险办理", "社保服务", "便民服务中心", 5, "身份证、户口簿", "启用"));

        var now = LocalDateTime.now();
        events.add(new Event(eventId.incrementAndGet(), "EV-2026001", "小区东门垃圾堆积", "环境卫生", "春风里第一网格", "居民上报", "垃圾未及时清运，影响通行", "待处理", now.minusHours(3), now.minusHours(3)));
        events.add(new Event(eventId.incrementAndGet(), "EV-2026002", "消防通道车辆占用", "公共安全", "幸福家园网格", "网格员巡查", "消防通道有车辆长期停放", "处理中", now.minusDays(1), now.minusHours(2)));
        events.add(new Event(eventId.incrementAndGet(), "EV-2026003", "路灯故障", "民生服务", "春风里第二网格", "居民上报", "9号楼旁路灯不亮", "已办结", now.minusDays(2), now.minusDays(1)));

        residents.add(new Resident(residentId.incrementAndGet(), "陈静", "女", "139****2261", "春风里第一网格", "春风路3号楼2单元", "常住人口"));
        residents.add(new Resident(residentId.incrementAndGet(), "赵建军", "男", "137****5210", "幸福家园网格", "幸福家园12号楼", "独居老人"));
        houses.add(new House(houseId.incrementAndGet(), "春风路3号楼2单元301", "春风里第一网格", "商品房", "自住", "陈静", 3));
        houses.add(new House(houseId.incrementAndGet(), "幸福家园12号楼101", "幸福家园网格", "商品房", "自住", "赵建军", 1));
        applications.add(new ServiceApplication(applicationId.incrementAndGet(), "SA-2026001", "高龄津贴申请", "赵建军", "137****5210", "材料齐全", "办理中", now.minusDays(1), now.minusHours(4)));
        users.add(new SystemUser(userId.incrementAndGet(), "admin", "系统管理员", "管理员", "138****0001", "启用"));
        users.add(new SystemUser(userId.incrementAndGet(), "grid_zhang", "张晓梅", "网格员", "138****1201", "启用"));
        persist();
    }

    public List<Grid> grids() { return grids; }
    public List<ServiceItem> serviceItems() { return serviceItems; }
    public List<Event> events() { return events; }
    public List<Resident> residents() { return residents; }
    public List<House> houses() { return houses; }
    public List<ServiceApplication> applications() { return applications; }
    public List<SystemUser> users() { return users; }
    public long nextGridId() { return gridId.incrementAndGet(); }
    public long nextServiceId() { return serviceId.incrementAndGet(); }
    public long nextEventId() { return eventId.incrementAndGet(); }
    public long nextResidentId() { return residentId.incrementAndGet(); }
    public long nextHouseId() { return houseId.incrementAndGet(); }
    public long nextApplicationId() { return applicationId.incrementAndGet(); }
    public long nextUserId() { return userId.incrementAndGet(); }

    public synchronized void persist() {
        save("grids", grids);
        save("serviceItems", serviceItems);
        save("events", events);
        save("residents", residents);
        save("houses", houses);
        save("applications", applications);
        save("users", users);
    }

    private void loadState() {
        grids.addAll(load("grids", new TypeReference<>() {}));
        serviceItems.addAll(load("serviceItems", new TypeReference<>() {}));
        events.addAll(load("events", new TypeReference<>() {}));
        residents.addAll(load("residents", new TypeReference<>() {}));
        houses.addAll(load("houses", new TypeReference<>() {}));
        applications.addAll(load("applications", new TypeReference<>() {}));
        users.addAll(load("users", new TypeReference<>() {}));
    }

    private <T> List<T> load(String key, TypeReference<List<T>> type) {
        var values = jdbcTemplate.query("SELECT state_value FROM app_state WHERE state_key = ?",
                (resultSet, rowNum) -> resultSet.getString(1), key);
        if (values.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(values.get(0), type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("无法读取 SQLite 中的业务数据: " + key, exception);
        }
    }

    private void save(String key, Object value) {
        try {
            var json = objectMapper.writeValueAsString(value);
            jdbcTemplate.update("""
                    INSERT INTO app_state(state_key, state_value) VALUES (?, ?)
                    ON CONFLICT(state_key) DO UPDATE SET state_value = excluded.state_value
                    """, key, json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("无法保存业务数据: " + key, exception);
        }
    }

    private void resetSequences() {
        gridId.set(maxId(grids.stream().map(Grid::id).toList()));
        serviceId.set(maxId(serviceItems.stream().map(ServiceItem::id).toList()));
        eventId.set(maxId(events.stream().map(Event::id).toList()));
        residentId.set(maxId(residents.stream().map(Resident::id).toList()));
        houseId.set(maxId(houses.stream().map(House::id).toList()));
        applicationId.set(maxId(applications.stream().map(ServiceApplication::id).toList()));
        userId.set(maxId(users.stream().map(SystemUser::id).toList()));
    }

    private long maxId(List<Long> ids) {
        return ids.stream().mapToLong(Long::longValue).max().orElse(0);
    }
}
