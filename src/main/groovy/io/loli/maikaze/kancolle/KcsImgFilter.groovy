package io.loli.maikaze.kancolle;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpSession;
import java.net.URL
import java.util.stream.Collectors;

/**
 * Created by uzuma on 2017/8/22.
 */
@Component
public class KcsImgFilter extends ZuulFilter {

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
        return ctx.getRequest().getRequestURI().startsWith("/kcs/");
    }

    @Override
    public Object run() {
        def loginContext = session.getAttribute("lctx")
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
            def worldUrl = "http://" + loginContext.$5_world_ip

            if (ctx.getRequest().getRequestURI().endsWith(".png")) {
                def imgPath = loginContext.$5_world.split("\\.").toList().stream()
                        .mapToInt({ Integer.parseInt(it) })
                        .mapToObj({ String.format("%03d", it) })
                        .collect(Collectors.joining("_"))
                URL url = UriComponentsBuilder.fromHttpUrl(worldUrl).path(imgPath)
                        .port(new Integer(80))
                        .build().toUri().toURL();
                ctx.setRouteHost(url);

            } else {
                def map = new LinkedMultiValueMap<>();
                ctx.getRequest().getParameterMap().each {map.add(it.key,it.value[0])}
                URL url = UriComponentsBuilder.fromHttpUrl(worldUrl).path(ctx.getRequest().getRequestURI())
                        .port(new Integer(80))
                        .queryParams(map)
                        .build().toUri().toURL();
                ctx.setRouteHost(url);
            }
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
    }

}
