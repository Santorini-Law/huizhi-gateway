package com.huizhi.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * base global filter
 *
 * @author LDZ
 * @date 2020/3/21 2:10 下午
 */
public abstract class BaseGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = new ArrayList<>();


    public boolean skipGlobalFilter(ServerWebExchange exchange) {
        String requestPath = exchange.getRequest().getPath().value();
        // 白名单 过滤掉
        return WHITE_LIST.contains(requestPath);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (skipGlobalFilter(exchange)) {
            return chain.filter(exchange);
        }
        return filterDecorator(exchange, chain);
    }


    /**
     * 装饰器
     *
     * @param exchange 过滤装饰器
     * @param chain    网关过滤链
     * @return Mono<void>
     */
    public abstract Mono<Void> filterDecorator(ServerWebExchange exchange, GatewayFilterChain chain);


}
