package io.loli.maikaze.kancolle

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.loli.maikaze.service.DmmAccountService
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.util.stream.Collectors

/**
 * Created by chocotan on 2017/8/22.
 */
@Component
class DmmLoginUserHelper {

    private static Cache<Long, DmmLogin> contextCache = Caffeine.newInstance()
            .build();

    @Autowired
    KancolleProperties kcp;

    @Autowired
    DmmAccountService dmmAccountService;


    def getServer(String token) {
        dmmAccountService.findByApiToken(token).serverIp
    }


    // 获取当前请求所对应的舰娘ip
    // 之所以没有
    // 当是api调用的时候，直接从token-server缓存中取
    // 当是静态资源的时候，从user-server缓存中取
    def getServer(HttpServletRequest request) {
        def apiToken = request.getParameter("api_token")
        if (apiToken) {
            getServer(apiToken)
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            dmmAccountService.findServerByUser(authentication.principal.user)
        }
    }


    // 获取所要登录的舰娘账号的LoginContext类
    public DmmLogin get(Long id, HttpSession session, boolean clear = false) {
        if (session && session.getAttribute("lctx_$id")) {
            return contextCache.get(id, { session.getAttribute("lctx_$id") });
        }

        HttpClientUtil httpClientUtil = kcp.proxy ? new HttpClientUtil(new Proxy(Proxy.Type.valueOf(kcp.proxyType), new InetSocketAddress(kcp.proxyIp, kcp.proxyPort))) :
                new HttpClientUtil();
        return contextCache.get(id, {
            new DmmLogin(properties: kcp, httpClientUtil: httpClientUtil)
        })
    }


    def getRouteUrl(HttpServletRequest request) {
        String originUri = request.getRequestURI()
        if (originUri.contains("/kcs/resources/image/world/") && originUri.endsWith(".png")) {
            def worldIp = getServer(request)
            String ip = worldIp
            def newUrl = Arrays.stream(ip.split("\\.")).mapToInt({ Integer.parseInt(it) })
                    .mapToObj({ String.format("%03d", it) })
                    .collect(Collectors.joining("_"))
            def nu = originUri.replaceAll("/kcs/resources/image/world/.+", "/kcs/resources/image/world/" + newUrl + "_t.png")
            return nu;
        } else {
            originUri
        }
    }
}
