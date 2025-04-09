package org.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.xuecheng.content.model.po.CourseCategory;
import org.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //使用递归查询分类
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);

}
