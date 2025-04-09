package org.xuecheng.content.service;

import com.alibaba.fastjson.JSON;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xuecheng.content.config.MultipartSupportConfig;
import org.xuecheng.content.feignclient.MediaServiceClient;
import org.xuecheng.content.mapper.CourseBaseMapper;
import org.xuecheng.content.mapper.CourseMarketMapper;
import org.xuecheng.content.mapper.CoursePublishMapper;
import org.xuecheng.content.mapper.CoursePublishPreMapper;
import org.xuecheng.content.model.dto.CourseBaseInfoDto;
import org.xuecheng.content.model.dto.CoursePreviewDto;
import org.xuecheng.content.model.dto.TeachplanDto;
import org.xuecheng.content.model.po.CourseBase;
import org.xuecheng.content.model.po.CourseMarket;
import org.xuecheng.content.model.po.CoursePublish;
import org.xuecheng.content.model.po.CoursePublishPre;
import org.xuecheng.exception.CommonError;
import org.xuecheng.exception.XueChengPlusException;
import org.xuecheng.messagesdk.model.po.MqMessage;
import org.xuecheng.messagesdk.service.MqMessageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @description 课程发布相关接口实现
 * @date 2023/2/21 10:04
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    //    课程预览接口调用服务实现
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程基本信息,营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }


    //    课程提交审核接口调用服务实现
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程找不到");
        }
        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        //如果课程的审核状态为已提交则不允许提交
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交请等待审核");
        }
        //本机构只能提交本机构的课程
        //todo:本机构只能提交本机构的课程

        //课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请求上传课程图片");
        }
        //查询课程计划
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("请编写课程计划");
        }

        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //设置机构id
        coursePublishPre.setCompanyId(companyId);

        //获取营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        //计划信息
        //转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);

        //状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");//审核状态为已提交
        courseBaseMapper.updateById(courseBase);
    }


    //    课程发布接口调用服务实现
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }
        //查询课程审核结果
        String status = coursePublishPre.getStatus();
        //课程如果没有审核通过不允许发布
        if (!status.equals("202004")) {
            XueChengPlusException.cast("课程没有审核通过不允许发布");
        }

        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //先查询课程发布，如果有则更新，没有再添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表mq_message写入数据
        saveCoursePublishMessage(courseId);

        //将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * @param courseId 课程id
     * @description 将要发布的课程信息保存消息表mq_message，等待调度中心分派执行器处理
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    //    课程发布后的分布式事务控制之课程页面静态化Step1
    @Override
    public File generateCourseHtml(Long courseId) {

        Configuration configuration = new Configuration(Configuration.getVersion());
        //最终的静态文件
        File htmlFile = null;
        try {
            //通过磁盘目录的方式得到模板所在目录，一旦达成可执行jar包部署到服务器上就找不到该目录地址了
            // 应该用下面217行流的方式目录在部署服务器上的地址
            //拿到classpath路径
//            String classpath = this.getClass().getResource("/").getPath();
            //指定模板的目录
//            configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));

            //改用流的方式确定模板的目录
            configuration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates"));

            //指定编码
            configuration.setDefaultEncoding("utf-8");
            //获取模板文件
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile = File.createTempFile("coursepublish", ".html");
            //输出文件
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("页面静态化出现问题,课程id:{}", courseId, e);
            e.printStackTrace();
        }
        return htmlFile;
    }


    //    课程发布后的分布式事务控制之课程页面静态化Step2
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            //将file转成MultipartFile
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            //远程调用得到返回值
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.debug("远程调用走降级逻辑得到上传的结果为null,课程id:{}", courseId);
                XueChengPlusException.cast("上传静态文件过程中存在异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件过程中存在异常");
        }
    }


    /**
     * 根据课程id查询课程发布信息
     *
     * @param courseId
     * @return 已发布课程的信息
     */
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }


    /**
     * 使用redis缓存优化后的根据课程id查询课程发布信息的接口
     * @param courseId
     * @return 已发布课程的信息
     */
    // 解决缓存穿透方法：查询请求校验/布隆过滤器/Guava/分布式锁（redisson 或 SETNX+lua脚本）
    // 缓存雪崩解决办法：同步锁（性能差）/ 缓存预热：使用专门的后台程序来定时将数据库中的数据提前存入缓存
    // 缓存击穿解决办法：分布式锁（Redisson（通过watchdog线程来自动延长锁的有效时间，能够比较精确地确定锁的有效时间，最大限度提高性能））或者SETNX（难以确定锁的有效时间，过长影响效率，过短不利于长时间任务执行））
    public CoursePublish getCoursePublishFromCache(Long courseId) {

        //从缓存中查询
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        //缓存中有
        if (jsonObj != null) {
//            System.out.println("=============从缓存中查询=============");
            //缓存中有直接返回数据
            String jsonString = jsonObj.toString();
            if ("null".equals(jsonString)) {
                // 对于不存在数据库中的查询，为避免缓存穿透，可以设置该查询的value为"null"或特殊值，并存入redis缓存
                // 该键值对需要设置在redis中的有效时间
                // 解决缓存穿透的其他方法：查询请求校验/布隆过滤器/Guava/分布式锁（redisson或SETNX）
                return null;
            }
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        } else {
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取分布式锁，实现方式主要有三种：基于数据库的乐观锁/SETNX/set nx/Redisson/zookeeper
            //客服同步锁无法解决分布在不同服务器上的多个微服务实例同时出现高并发访问数据库情况时所带来的缓存三兄弟问题
            lock.lock();   //自旋锁，其他没有得到锁的事务会反复请求锁
            try {
                //再次查询一下缓存，尽量减少并发事务访问数据库
                //从缓存中查询
                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
                //缓存中有
                if (jsonObj != null) {
                    //缓存中有直接返回数据
                    String jsonString = jsonObj.toString();
                    if ("null".equals(jsonString)) {
                        // 对于不存在数据库中的查询，为避免缓存穿透，可以设置该查询的value为"null"或特殊值，并存入redis缓存
                        // 该键值对需要设置在redis中的有效时间
                        // 解决缓存穿透的其他方法：Guava/redisson/分布式锁
                        return null;
                    }
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }

                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                //查询完成再存储到redis
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300, TimeUnit.SECONDS);
//                为redis缓存中存储的键值对设置不同的有效时间，避免出现大量key同时失效的现象，从而解决缓存雪崩问题；但是设置有效时间会带来其他问题：缓存击穿
//                缓存雪崩其他解决办法：同步锁（性能差）/ 缓存预热：使用专门的后台程序来定时将数据库中的数据提前存入缓存
//                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300 + new Random().nextInt(100), TimeUnit.SECONDS);
                return coursePublish;

            } finally {
                //手动释放锁
                lock.unlock();
            }
        }
    }


}