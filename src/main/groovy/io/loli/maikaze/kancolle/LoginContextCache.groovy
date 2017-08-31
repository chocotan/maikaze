package io.loli.maikaze.kancolle

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.servlet.http.HttpSession
/**
 * Created by chocotan on 2017/8/22.
 */
@Component
class LoginContextCache {

    private static Cache<Long, LoginContext> contextCache = Caffeine.newInstance()
            .build();


    @Autowired
    KancolleProperties kcp;

    public LoginContext get(Long id,  HttpSession session, boolean clear=false) {
        if (session && session.getAttribute("lctx")) {
            return contextCache.get(id, { session.getAttribute("lctx") });
        }

        HttpClientUtil httpClientUtil = kcp.proxy ? new HttpClientUtil(new Proxy(Proxy.Type.valueOf(kcp.proxyType), new InetSocketAddress(kcp.proxyIp, kcp.proxyPort))) :
                new HttpClientUtil();
        return contextCache.get(id, {
            new LoginContext(properties: kcp, httpClientUtil: httpClientUtil)
        })
    }

}
