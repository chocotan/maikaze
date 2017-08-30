package io.loli.maikaze.service

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.domains.User
import io.loli.maikaze.repository.DmmAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service
import sun.plugin.liveconnect.SecurityContextHelper

/**
 * Created by chocotan on 2017/8/30.
 */
@Service
class DmmAccountService {
    @Autowired
    DmmAccountRepository repository;


    def save(DmmAccount dmmAccount){
        repository.save(dmmAccount)
    }

    def findByUser(User user) {
        repository.findByUser user
    }
}
