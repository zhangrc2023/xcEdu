package org.xuecheng.learning.service;


import org.xuecheng.learning.model.dto.XcChooseCourseDto;
import org.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author Mr.M
 * @version 1.0
 * @description 选课相关的接口
 * @date 2023/2/25 12:00
 */
public interface MyCourseTablesService {

    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return 选课信息com.xuecheng.learning.model.dto.XcChooseCourseDto
     * @description 用户添加选课的处理方法
     * @date 2022/10/24 17:33
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @description 判断学习资格
     * @author Mr.M
     * @date 2022/10/3 7:37
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功状态
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

}
