package com.huizhi.gateway.config;

import com.huizhi.gateway.convert.CustomJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

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
                .pathMatchers("/admin/login/**", "/admin/**").hasRole("admin")
                .anyExchange().authenticated();

        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(converter())
        ;
        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> converter() {
        return new ReactiveJwtAuthenticationConverterAdapter(new CustomJwtAuthenticationConverter());
    }

}
