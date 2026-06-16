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
import com.grid.model.OperationLog;
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
    private final List<OperationLog> operationLogs = new ArrayList<>();
    private final AtomicLong gridId = new AtomicLong();
    private final AtomicLong serviceId = new AtomicLong();
    private final AtomicLong eventId = new AtomicLong();
    private final AtomicLong residentId = new AtomicLong();
    private final AtomicLong houseId = new AtomicLong();
    private final AtomicLong applicationId = new AtomicLong();
    private final AtomicLong userId = new AtomicLong();
    private final AtomicLong noticeId = new AtomicLong();
    private final AtomicLong logId = new AtomicLong();

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
            migrateModelFields();
            if (notices.isEmpty()) {
                var now = LocalDateTime.now();
                notices.add(new Notice(nextNoticeId(), "关于开展社区消防安全检查的通知", "社区通知",
                        "本周六上午将开展消防通道与公共设施安全检查，请居民配合。", null, "已发布", now.minusHours(5)));
                notices.add(new Notice(nextNoticeId(), "便民服务中心周末开放公告", "便民服务",
                        "本周末便民服务中心正常开放，办理时间为 9:00-16:00。", null, "已发布", now.minusDays(1)));
                persist();
            }
            if (users.stream().noneMatch(user -> "user".equals(user.username()))) {
                users.add(new SystemUser(nextUserId(), "user", "普通用户", passwordEncoder.encode("user123"),
                        "普通用户", "139****0002", "启用", LocalDateTime.now(), LocalDateTime.now()));
                persist();
            }
            return;
        }

        seedData();
        persist();
    }

    private void seedData() {
        var now = LocalDateTime.now();

        grids.add(new Grid(nextGridId(), "WG-001", "春风里第一网格", "春风里社区",
                "覆盖春风路1-8号楼", "张晓梅", "138****1201", 1260, 116.397428, 39.909204));
        grids.add(new Grid(nextGridId(), "WG-002", "春风里第二网格", "春风里社区",
                "覆盖春风路9-16号楼", "李建国", "138****1202", 980, 116.403428, 39.915204));
        grids.add(new Grid(nextGridId(), "WG-003", "幸福家园网格", "幸福家园社区",
                "幸福家园小区全域", "王芳", "138****1203", 1530, 116.410428, 39.920204));

        serviceItems.add(new ServiceItem(nextServiceId(), "低保申请", "民政服务", "社会事务办公室",
                "为符合条件的困难家庭提供最低生活保障申请服务", "身份证、户口簿、收入证明、财产申报表",
                "提交材料→社区初审→街道审核→公示→发放", "15个工作日", "启用"));
        serviceItems.add(new ServiceItem(nextServiceId(), "高龄津贴申请", "民政服务", "社会事务办公室",
                "为80周岁以上老年人提供高龄津贴申请", "身份证、户口簿、银行卡",
                "提交材料→社区核实→街道审批→发放", "10个工作日", "启用"));
        serviceItems.add(new ServiceItem(nextServiceId(), "养老保险办理", "社保服务", "便民服务中心",
                "城乡居民基本养老保险参保登记", "身份证、户口簿、照片2张",
                "提交材料→审核→缴费→领取", "5个工作日", "启用"));

        events.add(new Event(nextEventId(), "EV-2026001", "小区东门垃圾堆积", "环境卫生", "春风里第一网格",
                "居民上报", "user", "垃圾未及时清运，影响通行", null, "春风路东门入口处",
                116.397528, 39.909304, "待处理", null, null, now.minusHours(3), now.minusHours(3)));
        events.add(new Event(nextEventId(), "EV-2026002", "消防通道车辆占用", "公共安全", "幸福家园网格",
                "网格员巡查", null, "消防通道有车辆长期停放", null, "幸福家园12号楼北侧",
                116.411428, 39.921204, "处理中", null, null, now.minusDays(1), now.minusHours(2)));
        events.add(new Event(nextEventId(), "EV-2026003", "路灯故障", "民生服务", "春风里第二网格",
                "居民上报", "user", "9号楼旁路灯不亮，夜间出行不便", null, "春风路9号楼",
                116.403528, 39.915304, "已办结", null, "已联系物业维修，更换灯泡后恢复正常", now.minusDays(2), now.minusDays(1)));

        residents.add(new Resident(nextResidentId(), "陈静", "女", "110101199003150021", "139****2261",
                "春风里第一网格", 1L, "春风路3号楼2单元301", 1L, "常住人口", null));
        residents.add(new Resident(nextResidentId(), "赵建军", "男", "110101195208120018", "137****5210",
                "幸福家园网格", 3L, "幸福家园12号楼101", 2L, "独居老人", "需定期探访"));

        houses.add(new House(nextHouseId(), "HS-001", "春风路3号楼2单元301", "3号楼", "2单元", "301",
                "春风里第一网格", 1L, "商品房", "自住", "陈静", 3, 116.397428, 39.909204));
        houses.add(new House(nextHouseId(), "HS-002", "幸福家园12号楼101", "12号楼", "1单元", "101",
                "幸福家园网格", 3L, "商品房", "自住", "赵建军", 1, 116.411428, 39.921204));

        applications.add(new ServiceApplication(nextApplicationId(), "SA-2026001", 3L, 1L,
                "高龄津贴申请", "赵建军", "137****5210", "user", "材料齐全，符合条件", null,
                "办理中", null, now.minusDays(1), now.minusHours(4)));

        users.add(new SystemUser(nextUserId(), "admin", "系统管理员", passwordEncoder.encode("admin123"),
                "管理员", "138****0001", "启用", now, now));
        users.add(new SystemUser(nextUserId(), "grid_zhang", "张晓梅", passwordEncoder.encode("grid123"),
                "管理员", "138****1201", "启用", now, now));
        users.add(new SystemUser(nextUserId(), "user", "普通用户", passwordEncoder.encode("user123"),
                "普通用户", "139****0002", "启用", now, now));

        notices.add(new Notice(nextNoticeId(), "关于开展社区消防安全检查的通知", "社区通知",
                "本周六上午将开展消防通道与公共设施安全检查，请居民配合。", null, "已发布", now.minusHours(5)));
        notices.add(new Notice(nextNoticeId(), "便民服务中心周末开放公告", "便民服务",
                "本周末便民服务中心正常开放，办理时间为 9:00-16:00。", null, "已发布", now.minusDays(1)));
    }

    // ===== accessors =====
    public List<Grid> grids() { return grids; }
    public List<ServiceItem> serviceItems() { return serviceItems; }
    public List<Event> events() { return events; }
    public List<Resident> residents() { return residents; }
    public List<House> houses() { return houses; }
    public List<ServiceApplication> applications() { return applications; }
    public List<SystemUser> users() { return users; }
    public List<Notice> notices() { return notices; }
    public List<OperationLog> operationLogs() { return operationLogs; }
    public long nextGridId() { return gridId.incrementAndGet(); }
    public long nextServiceId() { return serviceId.incrementAndGet(); }
    public long nextEventId() { return eventId.incrementAndGet(); }
    public long nextResidentId() { return residentId.incrementAndGet(); }
    public long nextHouseId() { return houseId.incrementAndGet(); }
    public long nextApplicationId() { return applicationId.incrementAndGet(); }
    public long nextUserId() { return userId.incrementAndGet(); }
    public long nextNoticeId() { return noticeId.incrementAndGet(); }
    public long nextLogId() { return logId.incrementAndGet(); }

    public synchronized void persist() {
        save("grids", grids);
        save("serviceItems", serviceItems);
        save("events", events);
        save("residents", residents);
        save("houses", houses);
        save("applications", applications);
        save("users", users);
        save("notices", notices);
        save("operationLogs", operationLogs);
    }

    public void addLog(Long userId, String username, String operation, String module) {
        operationLogs.add(new OperationLog(nextLogId(), userId, username, operation, module, LocalDateTime.now()));
    }

    // ===== persistence helpers =====
    private void loadState() {
        grids.addAll(loadSafe("grids", new TypeReference<>() {}));
        serviceItems.addAll(loadSafe("serviceItems", new TypeReference<>() {}));
        events.addAll(loadSafe("events", new TypeReference<>() {}));
        residents.addAll(loadSafe("residents", new TypeReference<>() {}));
        houses.addAll(loadSafe("houses", new TypeReference<>() {}));
        applications.addAll(loadSafe("applications", new TypeReference<>() {}));
        users.addAll(loadSafe("users", new TypeReference<>() {}));
        notices.addAll(loadSafe("notices", new TypeReference<>() {}));
        operationLogs.addAll(loadSafe("operationLogs", new TypeReference<>() {}));
    }

    private <T> List<T> loadSafe(String key, TypeReference<List<T>> type) {
        try {
            return load(key, type);
        } catch (Exception e) {
            return List.of();
        }
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
        logId.set(maxId(operationLogs.stream().map(OperationLog::id).toList()));
    }

    private long maxId(List<Long> ids) {
        return ids.stream().mapToLong(Long::longValue).max().orElse(0);
    }

    // ===== migrations =====
    private void migratePasswords() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            var user = users.get(i);
            if (user.password() == null || user.password().isBlank()) {
                String defaultPassword = "admin".equals(user.username()) ? "admin123"
                        : "user".equals(user.username()) ? "user123" : "grid123";
                users.set(i, new SystemUser(user.id(), user.username(), user.displayName(),
                        passwordEncoder.encode(defaultPassword), user.role(), user.phone(), user.status(),
                        user.createTime(), user.updateTime()));
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
                        "管理员", user.phone(), user.status(), user.createTime(), user.updateTime()));
                changed = true;
            }
        }
        if (changed) persist();
    }

    private void migrateOwnership() {
        boolean changed = false;
        for (int i = 0; i < applications.size(); i++) {
            var item = applications.get(i);
            if (item.ownerUsername() == null && ("普通用户".equals(item.applicantName()) || "赵建军".equals(item.applicantName()))) {
                applications.set(i, new ServiceApplication(item.id(), item.code(), item.userId(), item.serviceItemId(),
                        item.itemName(), item.applicantName(), item.applicantPhone(), "user", item.content(),
                        item.attachment(), item.status(), item.handleResult(), item.createdAt(), item.updatedAt()));
                changed = true;
            }
        }
        for (int i = 0; i < events.size(); i++) {
            var item = events.get(i);
            if (item.ownerUsername() == null && "居民上报".equals(item.reporter())) {
                events.set(i, new Event(item.id(), item.code(), item.title(), item.category(), item.gridName(),
                        item.reporter(), "user", item.description(), item.imageUrl(), item.address(),
                        item.longitude(), item.latitude(), item.status(), item.handlerId(),
                        item.handleResult(), item.createdAt(), item.updatedAt()));
                changed = true;
            }
        }
        if (changed) persist();
    }

    private void migrateModelFields() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            var u = users.get(i);
            if (u.createTime() == null) {
                users.set(i, new SystemUser(u.id(), u.username(), u.displayName(), u.password(),
                        u.role(), u.phone(), u.status(), LocalDateTime.now(), LocalDateTime.now()));
                changed = true;
            }
        }
        for (int i = 0; i < notices.size(); i++) {
            var n = notices.get(i);
            if (n.status() == null) {
                notices.set(i, new Notice(n.id(), n.title(), n.type(), n.content(), n.publisherId(),
                        "已发布", n.publishedAt()));
                changed = true;
            }
        }
        if (changed) persist();
    }
}
