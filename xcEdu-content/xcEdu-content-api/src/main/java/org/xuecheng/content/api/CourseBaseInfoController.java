package org.xuecheng.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xuecheng.content.model.dto.AddCourseDto;
import org.xuecheng.content.model.dto.CourseBaseInfoDto;
import org.xuecheng.content.model.dto.EditCourseDto;
import org.xuecheng.content.model.dto.QueryCourseParamsDto;
import org.xuecheng.content.model.po.CourseBase;
import org.xuecheng.content.service.CourseBaseInfoService;
import org.xuecheng.content.util.SecurityUtil;
import org.xuecheng.exception.ValidationGroups;
import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;

import java.security.Principal;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description ToDoWhat
 * @date 2025/3/21 15:29 （日期和时间）
 */

@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//指定权限标识符,拥有此权限才可以访问此方法
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParamsDto) {

        //当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        //用户所属机构id
        Long companyId = null;
        if(StringUtils.isNotEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);
        return courseBasePageResult;
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    // 分组校验参数
//    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
    // @Validated 启动JSR303参数合法性校验注解
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        //获取到用户所属机构的id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId, Principal principal){
        //通过扩展工具类获取当前用户的身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user.getUsername());
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }


    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        //获取到用户所属机构的id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBaseInfoDto;
    }

}
