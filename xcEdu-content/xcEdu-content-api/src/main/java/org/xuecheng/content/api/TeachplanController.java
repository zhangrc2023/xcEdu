package org.xuecheng.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.xuecheng.content.model.dto.SaveTeachplanDto;
import org.xuecheng.content.model.dto.TeachplanDto;
import org.xuecheng.content.service.TeachplanService;

import java.util.List;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description 课程计划管理相关的接口
 * @date 2025/3/22 22:39 （日期和时间）
 */

@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        return teachplanTree;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

}
