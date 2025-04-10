package org.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xuecheng.content.model.dto.TeachplanDto;
import org.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanDto> selectTreeNodes(Long courseId);
}
