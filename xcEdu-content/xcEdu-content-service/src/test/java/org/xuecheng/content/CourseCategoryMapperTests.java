package org.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xuecheng.content.mapper.CourseCategoryMapper;
import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;
import org.xuecheng.content.mapper.CourseBaseMapper;
import org.xuecheng.content.mapper.CourseCategoryMapper;
import org.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.xuecheng.content.model.dto.QueryCourseParamsDto;
import org.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
