package io.loli.maikaze.kancolle

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpSession

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

/**
 * Created by chocotan on 2017/9/2.
 */
@Component
class Mobile2PcUserAgentFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(Mobile2PcUserAgentFilter.class);


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -3;
    }

    @Override
    public boolean shouldFilter() {
        true
    }

    def defaultUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36"


    def mobile2Pc = {
        // 移动端访问的时候修改user-agent为pc浏览器的
        if(!it){
            defaultUa
        }
        if (it.matches("(?i)(Android|BlackBerry|IEMobile|iPhone|iPad|iPod|Mobile)")) {
            log.info("Replace ua {} to {}", it, defaultUa)
            defaultUa
        } else {
            it
        }
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        def ua = ctx.getRequest().getHeader("User-Agent");
        ua = mobile2Pc(ua)
        ctx.set("UA", ua)
    }

}
