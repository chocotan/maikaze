package io.loli.maikaze.service

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.domains.User
import io.loli.maikaze.repository.DmmAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
/**
 * Created by chocotan on 2017/8/30.
 */
@Service
class DmmAccountService {
    @Autowired
    DmmAccountRepository repository;


    @Caching(
            put = [
                    @CachePut(value = "account", key = "'account_'+#dmmAccount.token"),
                    @CachePut(value = "account", key = "'account_'+#dmmAccount.id")
            ],
            evict = @CacheEvict(value = "account", key = "'server_'+#dmmAccount.user.id")
    )
    def save(DmmAccount dmmAccount) {
        repository.save(dmmAccount)
    }

    @Cacheable(value = "account", key = "'server_'+#user.id")
    def findServerByUser(User user) {
        repository.findByUser(user)
                .stream().filter({ it.serverIp != null }).map({ it.serverIp })
                .findFirst().orElse(null)
    }

    @Cacheable(value = "account", key = "'account_'+#apiToken")
    def findByApiToken(String apiToken) {
        repository.findByToken(apiToken);
    }


    @Caching(
            evict = [
                    @CacheEvict(value = "account", key = "'account_'+#id"),
                    @CacheEvict(value = "account", key = "'accountlist_'+#id")
            ]
    )
    def deleteById(Long id) {
        repository.delete(id)
    }



    def findByUser(User user) {
        repository.findByUser user
    }

    @Cacheable(value = "account", key = "'account_'+#id")
    def findById(Long id) {
        repository.findById id
    }




}
