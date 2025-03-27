package org.xuecheng.content.service;

import org.xuecheng.content.model.dto.CoursePreviewDto;

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
     * @description 生成课程页面静态文件
     * @param courseId  课程id
     * @return File 静态化文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */

    public File generateCourseHtml(Long courseId);
    /**
     * @description 远程调用media_service微服务上传课程静态化页面到minio
     * @param file  静态化文件
     * @return void
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public void  uploadCourseHtml(Long courseId, File file);
}
