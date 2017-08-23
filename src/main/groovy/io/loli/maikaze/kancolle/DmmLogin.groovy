package io.loli.maikaze.kancolle

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

/**
 * Created by uzuma on 2017/8/20.
 */
@RestController
class DmmLogin {


    @Autowired
    LoginContextCache loginContextCache;

    @RequestMapping("/dmmLogin")
    def getGameFlashUrl(String username, String password, HttpServletRequest request) {
        def lctx = loginContextCache.get(username, password, request.getSession(), true);
        request.session.setAttribute("lctx", lctx)
        lctx.user_agent = request.getHeader("User-Agent");
        lctx.reset(username, password)
        def flashUrl = lctx.startLogin();
        flashUrl.replace("http://" + lctx.$5_world_ip, "");
    }
}

