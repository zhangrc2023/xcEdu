package org.xuecheng.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xuecheng.ucenter.model.po.XcMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface XcMenuMapper extends BaseMapper<XcMenu> {

    //查看用户所属角色所拥有的权限，返回一个数据类型为XcMenu对象的列表
    @Select("SELECT	* FROM xc_menu WHERE id IN (SELECT menu_id FROM xc_permission WHERE role_id IN ( SELECT role_id FROM xc_user_role WHERE user_id = #{userId} ))")
    List<XcMenu> selectPermissionByUserId(@Param("userId") String userId);
}
