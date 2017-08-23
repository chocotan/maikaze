package io.loli.maikaze.kancolle

import ch.qos.logback.core.spi.ContextAware
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.servlet.http.HttpSession

/**
 * Created by uzuma on 2017/8/22.
 */
@Component
class LoginContextCache {

    private static Cache<String, LoginContext> contextCache = Caffeine.newInstance()
            .build();


    @Autowired
    KancolleProperties kcp;


    public LoginContext get(String username, String password, HttpSession session, def clear = false) {
        if (clear) {
            contextCache.invalidate("$username^^$password")
        }

        if (session && session.getAttribute("lctx")) {
            return contextCache.get("$username^^$password", { session.getAttribute("lctx") });
        }

        HttpClientUtil httpClientUtil = kcp.proxy ? new HttpClientUtil(new Proxy(Proxy.Type.valueOf(kcp.proxyType), new InetSocketAddress(kcp.proxyIp, kcp.proxyPort))) :
                new HttpClientUtil();
        return contextCache.get("$username^^$password", {
            new LoginContext(properties: kcp, httpClientUtil: httpClientUtil)
        })
    }

}
