package io.loli.maikaze

import io.loli.maikaze.kancolle.KancolleProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.cloud.netflix.zuul.EnableZuulServer
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableConfigurationProperties(KancolleProperties)
@EnableZuulServer
@ServletComponentScan
@EnableJpaRepositories
class MaikazeApplication {

    static void main(String[] args) {
        SpringApplication.run MaikazeApplication, args
    }


    @Bean
    @ConditionalOnMissingBean
    ProxyRequestHelper proxyRequestHelper(){
        return new ProxyRequestHelper()
    }

}
