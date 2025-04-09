package org.xuecheng.search.service;

import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;
import org.xuecheng.search.dto.SearchCourseParamDto;
import org.xuecheng.search.dto.SearchPageResultDto;
import org.xuecheng.search.po.CourseIndex;

/**
 * @description 课程搜索service
 * @author Mr.M
 * @date 2022/9/24 22:40
 * @version 1.0
 */
public interface CourseSearchService {


    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.xuecheng.base.model.PageResult<po.search.org.xuecheng.CourseIndex> 课程列表
     * @author Mr.M
     * @date 2022/9/24 22:45
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

 }
