package org.xuecheng.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
* @author RC.Zhang
* @version 1.0
* @description 分页查询结果模型类
* @date 2025/3/21 15:15 （日期和时间）
*/

@AllArgsConstructor
@Data
@ToString
public class PageResult<T> implements Serializable {

    // 数据列表
    private List<T> items;

    //总记录数
    private long counts;

    //当前页码
    private long page;

    //每页记录数
    private long pageSize;

}
