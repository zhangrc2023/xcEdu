package org.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description 调用Search微服务向ES添加课程索引熔断后的降级处理类
 * @date 2023/2/22 14:50
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("添加课程索引发生熔断,索引信息:{},熔断异常:{}", courseIndex, throwable.toString(), throwable);
                //走降级了返回 false
                return false;
            }
        };
    }
}
