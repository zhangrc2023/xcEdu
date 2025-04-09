package org.xuecheng.search.controller;

import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;
import org.xuecheng.search.dto.SearchCourseParamDto;
import org.xuecheng.search.dto.SearchPageResultDto;
import org.xuecheng.search.po.CourseIndex;
import org.xuecheng.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description 课程搜索接口
 * @date 2022/9/24 22:31
 */
@Api(value = "课程搜索接口", tags = "课程搜索接口")
@RestController
@RequestMapping("/course")
public class CourseSearchController {

    @Autowired
    CourseSearchService courseSearchService;


    @ApiOperation("课程搜索列表")
    @GetMapping("/list")
    public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {

        return courseSearchService.queryCoursePubIndex(pageParams, searchCourseParamDto);

    }
}
