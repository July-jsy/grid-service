package com.grid.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grid.common.Result;
import com.grid.model.UserView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    private static final Set<String> ADMIN_PREFIXES = Set.of(
            "/api/dashboard", "/api/grids", "/api/base-info", "/api/users"
    );
    private final ObjectMapper objectMapper;

    public RoleInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var user = (UserView) request.getSession().getAttribute("user");
        if (user != null && "管理员".equals(user.role())) return true;

        var path = request.getRequestURI();
        var method = request.getMethod();
        boolean adminOnly = ADMIN_PREFIXES.stream().anyMatch(path::startsWith)
                || path.startsWith("/api/service-items") && !"GET".equals(method)
                || path.startsWith("/api/notices") && !"GET".equals(method)
                || path.matches("/api/service-applications/\\d+/status")
                || path.matches("/api/events/\\d+/status");
        if (!adminOnly) return true;

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail("当前账号无权访问该功能")));
        return false;
    }
}
