package io.loli.maikaze.kancolle;

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

/**
 * Created by chocotan on 2017/8/31.
 */
@Component
public class KcsCachePreFilter extends ZuulFilter {
    @Override
    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return -2
    }

    @Override
    boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        def url = ctx.getRequest().getRequestURI()
        url.matches(".*/scenes/.+swf(\\?(VERSION|version)=.+)?") ||
                url.matches(".*/kcs/sound/.*")||
                url.matches(".*/kcs/resources/.*")
    }


    @Autowired
    KcsCacheService kcsCacheUtil;


    @Override
    Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        def cache = kcsCacheUtil.get(req.requestURI)
        if (cache) {
            cache.headers.forEach({ key, value -> ctx.getResponse().setHeader(key, value) })
            ctx.getResponse().getOutputStream().write(cache.bytes);
            ctx.set("CACHE_HIT", true);
            return ctx.getResponse();
        }


        ctx.set("CACHE_HIT", false)
        return null
    }
}
