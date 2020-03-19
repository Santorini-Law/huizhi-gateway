package com.huizhi.gateway.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 自定义 JWT 认证转换器
 * Spring 只添加 scope jwt令牌中的内容
 * 忽略了authorities-的所有内容，
 * 因此除非我们将JwtAuthenticationConverter扩展为也添加来自令牌中的权限（或其他声明），
 * 否则它们无法在webflux资源服务器中使用。
 * 在安全配置中，添加了jwtAuthenticationConverter
 * 并且 在scope前没必要增加 SCOPE_
 *
 * @author LDZ
 * @date 2020-03-19 13:43
 */
public class CustomJwtAuthenticationConverter extends JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final Collection<String> WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES =
            // added authorities
            Arrays.asList("scope", "scp", "authorities");

    @Override
    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return customGetScopes(jwt)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Collection<String> customGetScopes(Jwt jwt) {
        Collection<String> authorities = new ArrayList<>();
        // add to collection instead of returning early
        for (String attributeName : WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES) {
            Object scopes = jwt.getClaims().get(attributeName);
            if (scopes instanceof String) {
                if (StringUtils.hasText((String) scopes)) {
                    authorities.addAll(Arrays.asList(((String) scopes).split(" ")));
                }
            } else if (scopes instanceof Collection) {
                authorities.addAll((Collection<String>) scopes);
            }
        }
        return authorities;
    }
}
