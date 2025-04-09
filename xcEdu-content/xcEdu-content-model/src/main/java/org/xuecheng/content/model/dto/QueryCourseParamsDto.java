package org.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description 课程查询条件类
 * @date 2025/3/21 15:18 （日期和时间）
 */

@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}
