package io.loli.maikaze.kancolle;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpSession;

@Component
public class KcsApiFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(KcsApiFilter.class);

    @Autowired
    private HttpSession session;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getRequest().getRequestURI().startsWith("/kcsapi/");
    }



    @Autowired
    DmmLoginUserHelper helper;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        def worldIp = helper.getServer(ctx.getRequest())
        try {
            def worldUrl = "http://" + worldIp
            URL url = UriComponentsBuilder.fromHttpUrl(worldUrl)
                    .build().toUri().toURL();
            ctx.setRouteHost(url);
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;
    }

}