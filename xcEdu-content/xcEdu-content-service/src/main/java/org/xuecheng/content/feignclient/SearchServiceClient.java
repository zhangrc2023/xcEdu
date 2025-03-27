package org.xuecheng.content.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @version 1.0
 * @description feign远程调用微服务接口
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
