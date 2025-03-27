package org.xuecheng.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xuecheng.content.model.dto.CoursePreviewDto;
import org.xuecheng.content.service.CoursePublishService;


/**
 * @description 课程发布控制器
 */
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    //    课程预览接口，视图模板为/templates/course_template.ftl
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        //查询课程的信息作为模型数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        //指定模型
        modelAndView.addObject("model", coursePreviewInfo);
        //指定模板
        modelAndView.setViewName("course_template");//根据视图名称加.ftl找到模板
        return modelAndView;
    }

    //    课程提交审核接口
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }


    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }


}
