package io.loli.maikaze.kancolle

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants
import org.springframework.stereotype.Component

import java.util.stream.Collectors;

/**
 * Created by chocotan on 2017/8/31.
 */
@Component
public class KcsCachePostFilter extends ZuulFilter {


    @Override
    String filterType() {
        return "post"
    }

    @Override
    int filterOrder() {
        return FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER + 1
    }

    @Override
    boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        def url = ctx.getRequest().getRequestURI()
        ctx.get("ROUTE_RESULT") &&(!ctx.getBoolean("CACHE_HIT")) && (url.matches(".*/scenes/.+swf(\\?(VERSION|version)=.+)?") ||
                url.matches(".*/kcs/sound/.*") ||
                url.matches(".*/kcs/resources/.*")||url.contains("/kcs/Core.swf"))
    }


    @Autowired
    KcsCacheService kcsCacheUtil;


    @Autowired
    DmmLoginUserHelper helper;


    @Override
    Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        def ba = IOUtils.toByteArray(ctx.getResponseDataStream());
        ctx.setResponseDataStream(new ByteArrayInputStream(ba))
        Map<String, String> headers = ctx.getOriginResponseHeaders().stream()
                .collect(Collectors.toMap({ it.first() },
                { it.second() })
        )

        def url = helper.getRouteUrl(ctx.getRequest())
        kcsCacheUtil.put(url, new KcsCacheObject(bytes: ba, headers: headers))
        return null

    }
}
