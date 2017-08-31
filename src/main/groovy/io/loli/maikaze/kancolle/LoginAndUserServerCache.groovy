package io.loli.maikaze.kancolle

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.loli.maikaze.service.DmmAccountService
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.util.stream.Collectors

/**
 * Created by chocotan on 2017/8/22.
 */
@Component
class LoginAndUserServerCache {

    private static Cache<Long, DmmLogin> contextCache = Caffeine.newInstance()
            .build();


    Cache<String, String> tokenAndServerCache = Caffeine.newInstance().build();

    Map<Long, List<String>> userServers;

    @Autowired
    KancolleProperties kcp;

    @Autowired
    DmmAccountService dmmAccountService;

    @PostConstruct
    @Scheduled(fixedRate = 600000L)
    def init() {
        def list = dmmAccountService.findAllAccount().stream()
                .filter({ it.token != null }).filter({it.serverIp!=null}).collect(Collectors.toList()
        )

        tokenAndServerCache.asMap().keySet().minus(list.stream().map({ it.token }).collect(Collectors.toList()))
                .forEach({ tokenAndServerCache.invalidate(it) })
        list.forEach({ tokenAndServerCache.put(it.token, it.serverIp) })

        userServers = list.stream().filter({ it.token != null })
                .collect(Collectors.groupingBy({ it.user.id }, Collectors.mapping({ it.serverIp }, Collectors.toList())))
    }


    def getServer(String token) {
        tokenAndServerCache.getIfPresent(token)
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
            def id = authentication.principal.id
            userServers.get(id).stream().findFirst().get()
        }
    }

    def putServer(Long userId, String token, String server) {
        def servers = userServers.computeIfAbsent(userId, { new ArrayList<>() })
        servers.add(server)
        tokenAndServerCache.put(token, server)
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

}
