package org.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description 内容管理服务启动类
 * @date 2025/3/21 15:37 （日期和时间）
 */
@EnableFeignClients(basePackages={"org.xuecheng.content.feignclient"})  //启动feign组件扫描注册
@EnableSwagger2Doc  // 生成swagger接口文档
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
