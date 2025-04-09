package org.xuecheng.content.service;

import org.xuecheng.content.model.dto.CoursePreviewDto;
import org.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @description 课程发布相关的接口
 */
public interface CoursePublishService {


    /**
     * @param courseId 课程id
     * @description 获取课程预览信息
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @param courseId 课程id
     * @description 提交审核
     */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口
     * @author Mr.M
     * @date 2022/9/20 16:23
     */
    public void publish(Long companyId, Long courseId);

    /**
     * @param courseId 课程id
     * @return File 静态化文件
     * @description 生成课程页面静态文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */

    public File generateCourseHtml(Long courseId);

    /**
     * @param file 上传课程页面静态化文件到minIO
     * @return void
     * @description 远程调用media_service微服务上传课程静态化页面到minio
     */
    public void uploadCourseHtml(Long courseId, File file);


    /**
     * 根据课程 id查询课程发布信息
     * @param courseId
     * @return
     */
    public CoursePublish getCoursePublish(Long courseId);


    /**
     * 使用redis缓存优化后的根据课程id查询课程发布信息的接口
     * @param courseId
     * @return 已发布课程的信息
     */
    public CoursePublish getCoursePublishFromCache(Long courseId);
}
