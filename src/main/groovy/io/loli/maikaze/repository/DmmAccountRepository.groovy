package io.loli.maikaze.repository

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.domains.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by chocotan on 2017/8/30.
 */
interface DmmAccountRepository extends JpaRepository<DmmAccount, Long> {
    List<DmmAccount> findByUser(User user)

    DmmAccount findById(Long id)
}
