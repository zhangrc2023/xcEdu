package org.xuecheng.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xuecheng.ucenter.model.po.XcUser;
import org.xuecheng.ucenter.service.WxAuthService;

import java.io.IOException;

/**
 * @description 用于接收微信第三方认证平台返回的授权码
 * @date 2023/2/24 15:13
 */
@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        //远程调用微信请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库

        XcUser xcUser = wxAuthService.wxAuth(code);

        if (xcUser == null) {
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";
    }
}
