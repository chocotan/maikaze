package io.loli.maikaze.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

/**
 * @author yewenlin
 */
@Entity
class DmmAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String username;
    String password;
    Date lastLogin;
    String token;
    String startTime;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    String serverIp;
    String lastFlashUrl;
}
