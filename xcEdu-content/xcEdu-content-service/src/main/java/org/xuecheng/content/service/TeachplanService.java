package org.xuecheng.content.service;


import org.xuecheng.content.model.dto.BindTeachplanMediaDto;
import org.xuecheng.content.model.dto.SaveTeachplanDto;
import org.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程计划管理相关接口
 * @date 2023/2/14 12:10
 */
public interface TeachplanService {
    /**
     * 根据课程id查询课程计划
     * @param courseId 课程计划
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);


    /**
     * 新增/修改/保存课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @description 教学计划绑定媒资
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
