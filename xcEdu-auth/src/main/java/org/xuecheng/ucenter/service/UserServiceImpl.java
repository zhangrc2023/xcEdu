package org.xuecheng.ucenter.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.xuecheng.ucenter.mapper.XcMenuMapper;
import org.xuecheng.ucenter.mapper.XcUserMapper;
import org.xuecheng.ucenter.model.dto.AuthParamsDto;
import org.xuecheng.ucenter.model.dto.XcUserExt;
import org.xuecheng.ucenter.model.po.XcMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/24 10:37
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;

    @Autowired
    ApplicationContext applicationContext;


//    已定义类UserServiceImpl并继承UserDetailsService接口
//    通过重写loadUserByUsername()方法实现连接数据库读取用户的信息完成认证
    //传入的请求认证的参数就是AuthParamsDto（会转为Json串），实现统一验证，返回用户存在数据库中zrc_users中的认证信息
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的json转成AuthParamsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }

        //认证类型，有password，wx。。。
        String authType = authParamsDto.getAuthType();
        //根据认证类型从spring容器取出指定的bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        //调用统一execute方法完成认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        //封装xcUserExt用户信息为UserDetails
        //根据UserDetails对象生成令牌
        UserDetails userPrincipal = getUserPrincipal(xcUserExt);

        return userPrincipal;
    }

    /**
     * @param xcUser 用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @description 查询用户信息
     */
    public UserDetails getUserPrincipal(XcUserExt xcUser){
        String password = xcUser.getPassword();
        //权限
        String[] authorities=  {"test"};
        //根据用户id查询用户的权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if(xcMenus.size()>0){
            List<String> permissions =new ArrayList<>();
            xcMenus.forEach(m->{
                //拿到了用户拥有的权限标识符
                permissions.add(m.getCode());
            });
            //将permissions转成数组
            authorities = permissions.toArray(new String[0]);
        }

        xcUser.setPassword(null);
        //将用户信息转json
        String userJson = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return  userDetails;
    }


}
