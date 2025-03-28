package org.xuecheng.ucenter.service;


import org.xuecheng.ucenter.model.po.XcUser;

/**
 * @description 微信扫码接入
 * @date 2023/2/24 15:42
 */
public interface WxAuthService {

    /**
     * 微信扫码认证需要完成的事：申请令牌，携带令牌查询用户信息、保存用户信息到数据库
     * @param code 微信下发的授权码
     * @return
     */
    public XcUser wxAuth(String code);
}
