package io.loli.maikaze.config

import org.springframework.stereotype.Component

import javax.servlet.*

/**
 * @author yewenlin
 */
@Component
public class UaFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        def session = servletRequest.getSession()
        if (session.getAttribute("UA") == null)
            session.setAttribute("UA",
                    Optional.ofNullable(servletRequest.getHeader("User-Agent")).orElse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36"));

        filterChain.doFilter(servletRequest, servletResponse)
    }

    @Override
    public void destroy() {
    }
}
