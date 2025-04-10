package org.xuecheng.content.service;

import org.xuecheng.content.model.dto.AddCourseDto;
import org.xuecheng.content.model.dto.CourseBaseInfoDto;
import org.xuecheng.content.model.dto.EditCourseDto;
import org.xuecheng.content.model.dto.QueryCourseParamsDto;
import org.xuecheng.content.model.po.CourseBase;
import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description ToDoWhat
 * @date 2025/3/21 20:40 （日期和时间）
 */

public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     * @param pageParams 分页查询参数
     * @param courseParamsDto 查询条件
     * @return 查询结果
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询信息
     * @param courseId 课程id
     * @return 课程详细信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);


    /**
     * 修改课程
     * @param companyId 机构id
     * @param editCourseDto 修改课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);
}
