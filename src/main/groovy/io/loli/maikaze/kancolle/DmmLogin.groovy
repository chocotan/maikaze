package io.loli.maikaze.kancolle

import javaslang.Tuple3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
/**
 * Created by chocotan on 2017/8/20.
 */
@Component
class DmmLogin {

    @Autowired
    LoginContextCache loginContextCache;

    def getGameFlashUrl(String username, String password, HttpServletRequest request) {
        def lctx = loginContextCache.get(username, password, request.getSession(), true);
        request.session.setAttribute("lctx", lctx)
        lctx.user_agent = request.getHeader("User-Agent");
        lctx.reset(username, password)
        def flashUrl = lctx.startLogin();
        Tuple3.of flashUrl.replace("http://" + lctx.$5_world_ip, "") ,lctx.$6_api_token,lctx.$6_api_starttime
    }
}

