package org.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    //    新增自定义数据库操作任务：从待处理人物列表media_process读取任务
    @Select("select * from media_process t where  t.id % #{shardTotal} = #{shardIndex} and (t.status=1 or t.status=3) and t.fail_count<3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal,
                                              @Param("shardIndex") int shardIndex,
                                              @Param("count") int count);


    /**
     * 通过改变status字段的值来实现乐观锁来使多个执行器需要竞争锁后才能执行任务，避免重复执行
     * 只有一个执行器能成功执行sql语句并获得大于0的返回值
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);

}
