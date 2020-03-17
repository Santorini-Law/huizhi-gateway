package com.huizhi.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 *
 * @author LDZ
 * @date 2020-03-16 13:54
 */
@Configuration
public class GatewayRoutes {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/ms/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://huizhi-user")
                )
                .route(r -> r.path("/ms/huizhi/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://huizhi-user")
                )

                .build();
    }
}
