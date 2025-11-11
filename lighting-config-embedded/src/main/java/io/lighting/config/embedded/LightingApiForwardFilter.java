package io.lighting.config.embedded;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Forwards external `/api/**` 请求到嵌入式 DispatcherServlet（挂载在 `/lighting-config`）,
 * 以保持对外 REST 路径不变。
 */
class LightingApiForwardFilter implements Filter {

    private final String targetPrefix;

    LightingApiForwardFilter(String targetPrefix) {
        this.targetPrefix = targetPrefix;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String relativePath = requestUri.substring(contextPath.length());
        if (relativePath.startsWith(targetPrefix)) {
            chain.doFilter(request, response);
            return;
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(targetPrefix + relativePath);
        dispatcher.forward(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
