package com.huizhi.gateway.filter;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 打印
 *
 * @author LDZ
 * @date 2020/3/21 4:40 下午
 */
@Service
@Slf4j
public class PrintApiResponseBodyGlobalFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_BODY = "request_body";

    public static final String RESPONSE_BODY = "response_body";

    public static final String REMOTE_ADDR = "remote_addr";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest serverHttpRequest = exchange.getRequest();

        String methodName = Objects.requireNonNull(serverHttpRequest.getMethod()).name();

        String requestPath = serverHttpRequest.getPath().value();

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(doFinally -> {
                    Map<String, String> requestBody = exchange.getAttribute(REQUEST_BODY);
                    String responseBody = (String) exchange.getAttributes().get(RESPONSE_BODY);

                    final Map<String, String> queryParamsMap = serverHttpRequest.getQueryParams().toSingleValueMap();

                    Map<String, Object> ycRequestParam = new HashMap<>(16);

                    try {
                        //大数据查询要
                        ycRequestParam.putAll(queryParamsMap);
                        if (null != serverHttpRequest.getHeaders() && null != requestBody) {
                            //json 头
                            if (serverHttpRequest.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
                                ycRequestParam.putAll(requestBody);
                            } else if (serverHttpRequest.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                                ycRequestParam.putAll(requestBody);
                            }
                            //其余的头就不处理了
                        }
                    } catch (Exception ex) {
                        log.error("e", ex);
                    }


                    // 实例地址
                    String remoteAddr = (String) exchange.getAttributes().get(REMOTE_ADDR);

                    try {
                        // 转移ycLog 大数据用
                        Map<String, List<Object>> ycLogRequestParam = new HashMap<>();
                        ycRequestParam.forEach((key, value) -> ycLogRequestParam.put(key, Lists.newArrayList(value)));
                        Map<String, String> requestMap = new HashMap<>();
                        requestMap.put("request", requestPath);
                        requestMap.put("requestParams", new Gson().toJson(ycLogRequestParam));
                        requestMap.put("response", responseBody);
                        requestMap.put("startTime", startTime + "");
                        requestMap.put("endTime", System.currentTimeMillis() + "");
                    } catch (Exception ex) {
                        log.error("parse ex ", ex);
                    }

                    long costTime = System.currentTimeMillis() - startTime;

                    /**
                     * 日志查询用
                     */
                    Map<String, String> requestMap = new HashMap<>();
                    requestMap.put("url", requestPath);
                    requestMap.put("method", methodName);
                    requestMap.put("allParams", new Gson().toJson(ycRequestParam));
                    requestMap.put("queryParams", new Gson().toJson(queryParamsMap));
                    requestMap.put("requestBody", new Gson().toJson(requestBody));
                    requestMap.put("responseBody", responseBody);
                    requestMap.put("costTime", costTime + "");
                    requestMap.put("remoteAddr", remoteAddr);


                    if (log.isInfoEnabled()) {
                        log.info("{} {} {} {}ms", remoteAddr, methodName, requestPath, costTime);
                    }
                    if (log.isInfoEnabled()) {
                        log.info("" +
                                        "\n url {} " +
                                        "\n method {}" +
                                        "\n query params {}" +
                                        "\n request {}" +
                                        "\n response {}" +
                                        "\n remoteAddr {}" +
                                        "\n cost time {} ",
                                requestPath,
                                methodName,
                                queryParamsMap,
                                requestBody,
                                responseBody,
                                remoteAddr,
                                costTime);
                    }
                }).doOnError((err) -> log.error("api-gateway error", err));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
