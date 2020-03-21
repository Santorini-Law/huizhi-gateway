package com.huizhi.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求参数处理
 *
 * @author LDZ
 * @date 2020/3/21 2:37 下午
 */
@Service
@Slf4j
public class HandleRequestParamGlobalFilter extends BaseGlobalFilter {

    @Override
    public Mono<Void> filterDecorator(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        final URI uri = serverHttpRequest.getURI();
        String queryString = uri.getRawQuery();
        if (null != (queryString)) {

            Map<String, String> queryMap = parseQueryStringNoDecode(queryString);
            boolean isExistChar = queryMap.keySet().stream().anyMatch(k -> k.endsWith("[]"));

            if (isExistChar) {
                Map<String, String> newMap = new HashMap<>(16);
                queryMap.forEach((k, v) -> {
                    if ("[]".endsWith(k)) {
                        try {
                            String newK = URLEncoder.encode(k, "UTF-8");
                            newMap.put(newK, v);
                        } catch (UnsupportedEncodingException e) {
                            newMap.put(k, v);
                        }
                    } else {
                        newMap.put(k, v);
                    }
                });

                String proxyUrl = newMap.entrySet().stream()
                        .map(p -> p.getKey() + "=" + p.getValue())
                        .reduce((p1, p2) -> p1 + "&" + p2).orElse("");
                try {
                    final URI proxyUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), proxyUrl, uri.getFragment());
                    ServerHttpRequest proxyServerHttpRequest = serverHttpRequest
                            .mutate()
                            .uri(proxyUri)
                            .build();
                    return chain.filter(exchange.mutate().request(proxyServerHttpRequest).build());
                } catch (Exception e) {
                    log.error("err", e);
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1000;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(URLEncoder.encode("[]", "UTF-8"));
    }

    // ============================== private ==============================

    private static final Pattern QUERY_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    public static Map<String, String> parseQueryStringNoDecode(String queryString) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (null != queryString) {
            Matcher matcher = QUERY_PATTERN.matcher(queryString);
            while (matcher.find()) {
                String name = matcher.group(1);
                String eq = matcher.group(2);
                String value = matcher.group(3);
                value = (value != null ? value : (StringUtils.hasLength(eq) ? "" : null));
                queryParams.add(name, value);
            }
        }
        return queryParams.toSingleValueMap();
    }
}
