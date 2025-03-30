package org.xuecheng.learning.service;

import org.xuecheng.model.RestResponse;

/**
 * @version 1.0
 * @description 在线学习相关的接口
 */
public interface LearningService {

    /**
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @param mediaId     视频文件id
     * @return com.xuecheng.base.model.RestResponse<java.lang.String>
     * @description 获取教学视频
     */
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);

}
