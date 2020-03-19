package com.huizhi.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * 查看用户详情
 *
 * @author LDZ
 * @date 2020-03-19 10:34
 */
@RestController
public class OauthUserInfoController {

    @GetMapping("/user")
    public Principal user(Principal user) {
        return user;
    }


    @GetMapping("/admin/info")
    public String adminInfo() {
        return "hello admin";
    }
}
