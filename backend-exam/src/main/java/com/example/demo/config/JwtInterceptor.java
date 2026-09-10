package com.example.demo.config;

import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.util.JsonUtil;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权 + 管理员角色校验拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return writeError(response, 401, "未登录或登录已过期");
        }

        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            if (handler instanceof HandlerMethod hm) {
                RequireAdmin ra = hm.getMethodAnnotation(RequireAdmin.class);
                if (ra == null) {
                    ra = hm.getBeanType().getAnnotation(RequireAdmin.class);
                }
                if (ra != null && (role == null || role != 0)) {
                    return writeError(response, 403, "无权限，仅管理员可操作");
                }
            }
            return true;
        } catch (Exception e) {
            return writeError(response, 401, "未登录或登录已过期");
        }
    }

    private boolean writeError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJson(Result.error(code, msg)));
        return false;
    }
}
