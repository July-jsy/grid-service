package com.grid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grid.entity.AppState;
import com.grid.mapper.AppStateMapper;
import com.grid.model.Event;
import com.grid.model.Grid;
import com.grid.model.House;
import com.grid.model.Notice;
import com.grid.model.Resident;
import com.grid.model.ServiceApplication;
import com.grid.model.ServiceItem;
import com.grid.model.SystemUser;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AppStateMapper appStateMapper;
    private final PasswordEncoder passwordEncoder;
    private final List<Grid> grids = new ArrayList<>();
    private final List<ServiceItem> serviceItems = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final List<Resident> residents = new ArrayList<>();
    private final List<House> houses = new ArrayList<>();
    private final List<ServiceApplication> applications = new ArrayList<>();
    private final List<SystemUser> users = new ArrayList<>();
    private final List<Notice> notices = new ArrayList<>();
    private final AtomicLong gridId = new AtomicLong();
    private final AtomicLong serviceId = new AtomicLong();
    private final AtomicLong eventId = new AtomicLong();
    private final AtomicLong residentId = new AtomicLong();
    private final AtomicLong houseId = new AtomicLong();
    private final AtomicLong applicationId = new AtomicLong();
    private final AtomicLong userId = new AtomicLong();
    private final AtomicLong noticeId = new AtomicLong();

    public DataStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, AppStateMapper appStateMapper,
                     PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.appStateMapper = appStateMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS app_state (state_key TEXT PRIMARY KEY, state_value TEXT NOT NULL)");
        loadState();
        if (!grids.isEmpty()) {
            resetSequences();
            migratePasswords();
            migrateRoles();
            migrateOwnership();
            if (notices.isEmpty()) {
                var now = LocalDateTime.now();
                notices.add(new Notice(nextNoticeId(), "关于开展社区消防安全检查的通知", "社区通知",
                        "本周六上午将开展消防通道与公共设施安全检查，请居民配合。", now.minusHours(5)));
                notices.add(new Notice(nextNoticeId(), "便民服务中心周末开放公告", "便民服务",
                        "本周末便民服务中心正常开放，办理时间为 9:00-16:00。", now.minusDays(1)));
                persist();
            }
            if (users.stream().noneMatch(user -> "user".equals(user.username()))) {
                users.add(new SystemUser(nextUserId(), "user", "普通用户", passwordEncoder.encode("user123"),
                        "普通用户", "139****0002", "启用"));
                persist();
            }
            return;
        }

        grids.add(new Grid(gridId.incrementAndGet(), "WG-001", "春风里第一网格", "春风里社区", "覆盖春风路1-8号楼", "张晓梅", "138****1201", 1260));
        grids.add(new Grid(gridId.incrementAndGet(), "WG-002", "春风里第二网格", "春风里社区", "覆盖春风路9-16号楼", "李建国", "138****1202", 980));
        grids.add(new Grid(gridId.incrementAndGet(), "WG-003", "幸福家园网格", "幸福家园社区", "幸福家园小区全域", "王芳", "138****1203", 1530));

        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "低保申请", "民政服务", "社会事务办公室", 15, "身份证、户口簿、收入证明", "启用"));
        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "高龄津贴申请", "民政服务", "社会事务办公室", 10, "身份证、银行卡", "启用"));
        serviceItems.add(new ServiceItem(serviceId.incrementAndGet(), "养老保险办理", "社保服务", "便民服务中心", 5, "身份证、户口簿", "启用"));

        var now = LocalDateTime.now();
        events.add(new Event(eventId.incrementAndGet(), "EV-2026001", "小区东门垃圾堆积", "环境卫生", "春风里第一网格", "居民上报", "user", "垃圾未及时清运，影响通行", null, "待处理", now.minusHours(3), now.minusHours(3)));
        events.add(new Event(eventId.incrementAndGet(), "EV-2026002", "消防通道车辆占用", "公共安全", "幸福家园网格", "网格员巡查", null, "消防通道有车辆长期停放", null, "处理中", now.minusDays(1), now.minusHours(2)));
        events.add(new Event(eventId.incrementAndGet(), "EV-2026003", "路灯故障", "民生服务", "春风里第二网格", "居民上报", "user", "9号楼旁路灯不亮", null, "已办结", now.minusDays(2), now.minusDays(1)));

        residents.add(new Resident(residentId.incrementAndGet(), "陈静", "女", "139****2261", "春风里第一网格", "春风路3号楼2单元", "常住人口"));
        residents.add(new Resident(residentId.incrementAndGet(), "赵建军", "男", "137****5210", "幸福家园网格", "幸福家园12号楼", "独居老人"));
        houses.add(new House(houseId.incrementAndGet(), "春风路3号楼2单元301", "春风里第一网格", "商品房", "自住", "陈静", 3));
        houses.add(new House(houseId.incrementAndGet(), "幸福家园12号楼101", "幸福家园网格", "商品房", "自住", "赵建军", 1));
        applications.add(new ServiceApplication(applicationId.incrementAndGet(), "SA-2026001", "高龄津贴申请", "普通用户", "user", "137****5210", "材料齐全", "办理中", now.minusDays(1), now.minusHours(4)));
        users.add(new SystemUser(userId.incrementAndGet(), "admin", "系统管理员", passwordEncoder.encode("admin123"),
                "管理员", "138****0001", "启用"));
        users.add(new SystemUser(userId.incrementAndGet(), "grid_zhang", "张晓梅", passwordEncoder.encode("grid123"),
                "管理员", "138****1201", "启用"));
        users.add(new SystemUser(userId.incrementAndGet(), "user", "普通用户", passwordEncoder.encode("user123"),
                "普通用户", "139****0002", "启用"));
        notices.add(new Notice(noticeId.incrementAndGet(), "关于开展社区消防安全检查的通知", "社区通知",
                "本周六上午将开展消防通道与公共设施安全检查，请居民配合。", now.minusHours(5)));
        notices.add(new Notice(noticeId.incrementAndGet(), "便民服务中心周末开放公告", "便民服务",
                "本周末便民服务中心正常开放，办理时间为 9:00-16:00。", now.minusDays(1)));
        persist();
    }

    public List<Grid> grids() { return grids; }
    public List<ServiceItem> serviceItems() { return serviceItems; }
    public List<Event> events() { return events; }
    public List<Resident> residents() { return residents; }
    public List<House> houses() { return houses; }
    public List<ServiceApplication> applications() { return applications; }
    public List<SystemUser> users() { return users; }
    public List<Notice> notices() { return notices; }
    public long nextGridId() { return gridId.incrementAndGet(); }
    public long nextServiceId() { return serviceId.incrementAndGet(); }
    public long nextEventId() { return eventId.incrementAndGet(); }
    public long nextResidentId() { return residentId.incrementAndGet(); }
    public long nextHouseId() { return houseId.incrementAndGet(); }
    public long nextApplicationId() { return applicationId.incrementAndGet(); }
    public long nextUserId() { return userId.incrementAndGet(); }
    public long nextNoticeId() { return noticeId.incrementAndGet(); }

    public synchronized void persist() {
        save("grids", grids);
        save("serviceItems", serviceItems);
        save("events", events);
        save("residents", residents);
        save("houses", houses);
        save("applications", applications);
        save("users", users);
        save("notices", notices);
    }

    private void loadState() {
        grids.addAll(load("grids", new TypeReference<>() {}));
        serviceItems.addAll(load("serviceItems", new TypeReference<>() {}));
        events.addAll(load("events", new TypeReference<>() {}));
        residents.addAll(load("residents", new TypeReference<>() {}));
        houses.addAll(load("houses", new TypeReference<>() {}));
        applications.addAll(load("applications", new TypeReference<>() {}));
        users.addAll(load("users", new TypeReference<>() {}));
        notices.addAll(load("notices", new TypeReference<>() {}));
    }

    private <T> List<T> load(String key, TypeReference<List<T>> type) {
        var state = appStateMapper.selectById(key);
        if (state == null) return List.of();
        try {
            return objectMapper.readValue(state.getStateValue(), type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("无法读取 SQLite 中的业务数据: " + key, exception);
        }
    }

    private void save(String key, Object value) {
        try {
            var json = objectMapper.writeValueAsString(value);
            var state = new AppState(key, json);
            if (appStateMapper.selectById(key) == null) appStateMapper.insert(state);
            else appStateMapper.updateById(state);
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
        noticeId.set(maxId(notices.stream().map(Notice::id).toList()));
    }

    private long maxId(List<Long> ids) {
        return ids.stream().mapToLong(Long::longValue).max().orElse(0);
    }

    private void migratePasswords() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            var user = users.get(i);
            if (user.password() == null || user.password().isBlank()) {
                String defaultPassword = "admin".equals(user.username()) ? "admin123"
                        : "user".equals(user.username()) ? "user123" : "grid123";
                users.set(i, new SystemUser(user.id(), user.username(), user.displayName(),
                        passwordEncoder.encode(defaultPassword), user.role(), user.phone(), user.status()));
                changed = true;
            }
        }
        if (changed) persist();
    }

    private void migrateOwnership() {
        boolean changed = false;
        for (int i = 0; i < applications.size(); i++) {
            var item = applications.get(i);
            if (item.ownerUsername() == null && ("普通用户".equals(item.applicant()) || "赵建军".equals(item.applicant()))) {
                applications.set(i, new ServiceApplication(item.id(), item.code(), item.itemName(), item.applicant(),
                        "user", item.phone(), item.materialsNote(), item.status(), item.createdAt(), item.updatedAt()));
                changed = true;
            }
        }
        for (int i = 0; i < events.size(); i++) {
            var item = events.get(i);
            if (item.ownerUsername() == null && "居民上报".equals(item.reporter())) {
                events.set(i, new Event(item.id(), item.code(), item.title(), item.category(), item.gridName(),
                        item.reporter(), "user", item.description(), item.imageUrl(), item.status(), item.createdAt(), item.updatedAt()));
                changed = true;
            }
        }
        if (changed) persist();
    }

    private void migrateRoles() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            var user = users.get(i);
            if ("网格员".equals(user.role())) {
                users.set(i, new SystemUser(user.id(), user.username(), user.displayName(), user.password(),
                        "管理员", user.phone(), user.status()));
                changed = true;
            }
        }
        if (changed) persist();
    }
}
