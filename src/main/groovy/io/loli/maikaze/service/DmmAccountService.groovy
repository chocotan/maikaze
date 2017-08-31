package io.loli.maikaze.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.domains.User
import io.loli.maikaze.kancolle.KancolleProperties
import io.loli.maikaze.kancolle.LoginContext
import io.loli.maikaze.kancolle.LoginContextCache
import io.loli.maikaze.repository.DmmAccountRepository
import javaslang.Tuple3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpSession

/**
 * Created by chocotan on 2017/8/30.
 */
@Service
class DmmAccountService {
    @Autowired
    DmmAccountRepository repository;

    def save(DmmAccount dmmAccount) {
        repository.save(dmmAccount)
    }

    def findByUser(User user) {
        repository.findByUser user
    }

    def findById(Long id) {
        repository.findById id
    }

    def deleteById(Long id) {
        repository.delete(id)
    }


    private static Cache<String, LoginContext> contextCache = Caffeine.newInstance()
            .build();


    @Autowired
    KancolleProperties kcp;

    @Autowired
    HttpSession session;

    @Autowired
    LoginContextCache loginContextCache

    def login(Long id) {
        def account = findById(id)
        def lctx = loginContextCache.get(id, session);
        session.setAttribute("lctx", lctx)
        lctx.user_agent = session.getAttribute "UA"
        lctx.reset(account.username, account.password)
        def flashUrl = lctx.startLogin();
        account.token = lctx.$6_api_token
        account.startTime = lctx.$6_api_starttime
        account.lastLogin = new Date()
        save(account)
        Tuple3.of flashUrl.replace("http://" + lctx.$5_world_ip, ""), lctx.$6_api_token, lctx.$6_api_starttime
    }
}
