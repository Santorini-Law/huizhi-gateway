package com.huizhi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 资源处理相关
 *
 * @author LDZ
 * @date 2020-03-18 14:22
 */
@EnableWebFluxSecurity
public class ResourceServerConfigurer {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/admin/login/**").hasRole("admin")
                .anyExchange().authenticated();

        http.oauth2ResourceServer().jwt();
        return http.build();
    }
}
