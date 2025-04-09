package org.xuecheng.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author RC.Zhang
 * @version 1.0
 * @description 分页查询分页参数
 * @date 2025/3/21 15:12 （日期和时间）
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class PageParams {

    //当前页码
    @ApiModelProperty("页码")   // swagger接口文档ui
    private Long pageNo = 1L;

    //每页显示记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = 30L;

}
