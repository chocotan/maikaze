package io.loli.maikaze

import io.loli.maikaze.kancolle.KancolleProperties
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.web.context.annotation.SessionScope

@SpringBootApplication
@EnableConfigurationProperties(KancolleProperties)
@EnableZuulProxy
@ServletComponentScan
class MaikazeApplication {

    static void main(String[] args) {
        SpringApplication.run MaikazeApplication, args
    }


}
