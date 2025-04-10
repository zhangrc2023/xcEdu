package org.xuecheng.media.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xuecheng.media.model.po.MediaFiles;
import org.xuecheng.media.service.MediaFileService;
import org.xuecheng.model.RestResponse;

/**
 * @version 1.0
 * @description 视频播放接口
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

//  课程播放页面根据提供的视频url地址来请求将要播放视频资源
    @ApiOperation("课程预览接口")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        //查询媒资文件信息
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);

        if (mediaFiles == null) {
            return RestResponse.validfail("找不到视频");
        }
        //取出视频播放地址
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)) {
            return RestResponse.validfail("该视频正在处理中");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
