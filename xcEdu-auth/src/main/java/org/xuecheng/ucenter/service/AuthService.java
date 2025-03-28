package org.xuecheng.ucenter.service;


import org.xuecheng.ucenter.model.dto.AuthParamsDto;
import org.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @description 统一的认证接口，例如密码认证，短信认证，微信扫码认证。。。
 * @date 2023/2/24 11:55
 */
public interface AuthService {

    /**
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @description 认证方法
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
